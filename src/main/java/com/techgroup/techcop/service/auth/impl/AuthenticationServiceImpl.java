package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.dto.AuthResponse;
import com.techgroup.techcop.model.dto.AuthenticatedUserDto;
import com.techgroup.techcop.model.dto.GoogleTokenInfo;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.audit.AuditLogService;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.UUID;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private static final String GOOGLE_TOKEN_INFO_URL =
            "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationCodeService verificationCodeService;
    private final RestTemplate restTemplate;
    private final PasswordHashingService passwordHashingService;
    private final AuditLogService auditLogService;
    private final AuthIdentityService authIdentityService;
    private final String googleClientId;
    private final boolean refreshCookieSecure;
    private final Environment environment;

    public AuthenticationServiceImpl(CustomerRepository customerRepository,
                                     RoleRepository roleRepository,
                                     JwtService jwtService,
                                     AuthenticationManager authManager,
                                     VerificationCodeService verificationCodeService,
                                     RestTemplate restTemplate,
                                     PasswordHashingService passwordHashingService,
                                     AuditLogService auditLogService,
                                     AuthIdentityService authIdentityService,
                                     @Value("${google.client-id:}") String googleClientId,
                                     @Value("${app.security.refresh-cookie.secure:false}") boolean refreshCookieSecure,
                                     Environment environment) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.verificationCodeService = verificationCodeService;
        this.restTemplate = restTemplate;
        this.passwordHashingService = passwordHashingService;
        this.auditLogService = auditLogService;
        this.authIdentityService = authIdentityService;
        this.googleClientId = googleClientId;
        this.refreshCookieSecure = refreshCookieSecure;
        this.environment = environment;
    }

    @Override
    public String getGoogleClientId() {
        return googleClientId == null ? "" : googleClientId.trim();
    }

    @Override
    public boolean accountExists(String identifier, String channel) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(channel, VerificationChannel.EMAIL);
        return authIdentityService.accountExists(identifier, verificationChannel);
    }

    @Override
    public String login(String identifier, String password, String channel) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(channel, VerificationChannel.EMAIL);
        String authEmail = authIdentityService.resolveAuthenticationEmail(identifier, verificationChannel);

        upgradeLegacyPasswordIfNeeded(authEmail, password);

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authEmail, password)
            );
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Correo o contrasena incorrectos."
            );
        }

        Customer customer = customerRepository
                .findByCustomerEmail(authEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!customer.isAccountVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La cuenta aun no ha sido verificada.");
        }

        verificationCodeService.generateAndSendCode(
                customer,
                verificationChannel,
                VerificationPurpose.LOGIN
        );

        return "Verification code sent via " + verificationChannel.name();
    }

    private void upgradeLegacyPasswordIfNeeded(String email, String rawPassword) {
        customerRepository.findByCustomerEmail(email)
                .ifPresent(customer -> {
                    String storedPassword = customer.getCustomerPassword();
                    if (!passwordHashingService.isBcryptHash(storedPassword)
                            && passwordHashingService.matches(rawPassword, storedPassword)) {
                        customer.setCustomerPassword(passwordHashingService.encodeIfNeeded(storedPassword));
                        customerRepository.save(customer);
                    }
                });
    }

    @Override
    public AuthResponse verifyCode(String identifier,
                                   String channel,
                                   String code,
                                   HttpServletResponse response) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(channel, VerificationChannel.EMAIL);
        Customer customer = authIdentityService.getCustomerByIdentifier(identifier, verificationChannel);

        boolean valid = verificationCodeService.verifyCode(
                customer,
                code,
                verificationChannel,
                VerificationPurpose.LOGIN
        );

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }

        AuthResponse authResponse = createAuthResponse(customer, response, true);
        auditLogService.log(
                customer.getCustomerEmail(),
                "LOGIN_SUCCESS",
                "CUSTOMER",
                customer.getCustomerId() == null ? null : customer.getCustomerId().toString(),
                "Inicio de sesion completado mediante verificacion OTP"
        );
        return authResponse;
    }

    @Override
    public AuthResponse authenticateWithGoogle(String credential,
                                               HttpServletResponse response) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Google login is not configured");
        }

        GoogleTokenInfo tokenInfo = restTemplate.getForObject(
                GOOGLE_TOKEN_INFO_URL,
                GoogleTokenInfo.class,
                credential
        );

        if (tokenInfo == null || isBlank(tokenInfo.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google credential");
        }

        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Google credential audience mismatch");
        }

        if (!"true".equalsIgnoreCase(tokenInfo.getEmailVerified())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Google email is not verified");
        }

        Customer customer = customerRepository
                .findByCustomerEmail(tokenInfo.getEmail().trim().toLowerCase())
                .map(existing -> mergeGoogleProfile(existing, tokenInfo))
                .orElseGet(() -> createGoogleCustomer(tokenInfo));

        AuthResponse authResponse = createAuthResponse(customer, response, true);
        auditLogService.log(
                customer.getCustomerEmail(),
                "GOOGLE_LOGIN_SUCCESS",
                "CUSTOMER",
                customer.getCustomerId() == null ? null : customer.getCustomerId().toString(),
                "Inicio de sesion completado con Google OAuth"
        );
        return authResponse;
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No refresh token");
        }

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || jwtService.isTokenExpired(refreshToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String email = normalizeEmail(jwtService.extractUsername(refreshToken));

        Customer customer = customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return createAuthResponse(customer, null, false);
    }

    @Override
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(isRefreshCookieSecure())
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private Customer mergeGoogleProfile(Customer customer,
                                        GoogleTokenInfo tokenInfo) {
        boolean changed = false;

        if (isBlank(customer.getCustomerName()) && !isBlank(tokenInfo.getGivenName())) {
            customer.setCustomerName(tokenInfo.getGivenName().trim());
            changed = true;
        }

        if (isBlank(customer.getCustomerLastName()) && !isBlank(tokenInfo.getFamilyName())) {
            customer.setCustomerLastName(tokenInfo.getFamilyName().trim());
            changed = true;
        }

        if (customer.getRole() == null) {
            customer.setRole(resolveCustomerRole());
            changed = true;
        }

        if (!customer.isAccountVerified()) {
            customer.setAccountVerified(true);
            changed = true;
        }

        return changed ? customerRepository.save(customer) : customer;
    }

    private Customer createGoogleCustomer(GoogleTokenInfo tokenInfo) {
        Customer customer = new Customer();
        customer.setCustomerEmail(tokenInfo.getEmail().trim().toLowerCase());
        customer.setCustomerName(defaultIfBlank(tokenInfo.getGivenName(), tokenInfo.getName()));
        customer.setCustomerLastName(defaultIfBlank(tokenInfo.getFamilyName(), ""));
        customer.setCustomerPhoneNumber("");
        customer.setCustomerPassword(passwordHashingService.hashNewPassword(UUID.randomUUID().toString()));
        customer.setRole(resolveCustomerRole());
        customer.setAccountVerified(true);

        return customerRepository.save(customer);
    }

    private Role resolveCustomerRole() {
        return roleRepository.findByRoleName("ROLE_CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Rol no encontrado"
                ));
    }

    private AuthResponse createAuthResponse(Customer customer,
                                            HttpServletResponse response,
                                            boolean includeRefreshCookie) {
        String email = customer.getCustomerEmail();
        String role = customer.getRole().getRoleName();
        String accessToken = jwtService.generateAccessToken(email, role);

        if (includeRefreshCookie && response != null) {
            String refreshToken = jwtService.generateRefreshToken(email);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    .secure(isRefreshCookieSecure())
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Strict")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }

        return new AuthResponse(accessToken, mapAuthenticatedUser(customer));
    }

    private AuthenticatedUserDto mapAuthenticatedUser(Customer customer) {
        return new AuthenticatedUserDto(
                customer.getCustomerId(),
                customer.getCustomerName(),
                customer.getCustomerLastName(),
                customer.getCustomerEmail(),
                customer.getRole() != null ? customer.getRole().getRoleName() : ""
        );
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String primary, String fallback) {
        return isBlank(primary) ? (fallback == null ? "" : fallback.trim()) : primary.trim();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean isRefreshCookieSecure() {
        return refreshCookieSecure || environment.acceptsProfiles(Profiles.of("prod"));
    }
}
