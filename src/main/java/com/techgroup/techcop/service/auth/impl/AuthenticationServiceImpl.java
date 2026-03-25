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
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    private final PasswordEncoder passwordEncoder;
    private final String googleClientId;

    public AuthenticationServiceImpl(CustomerRepository customerRepository,
                                     RoleRepository roleRepository,
                                     JwtService jwtService,
                                     AuthenticationManager authManager,
                                     VerificationCodeService verificationCodeService,
                                     RestTemplate restTemplate,
                                     PasswordEncoder passwordEncoder,
                                     @Value("${google.client-id:}") String googleClientId) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.verificationCodeService = verificationCodeService;
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.googleClientId = googleClientId;
    }

    @Override
    public String login(String email, String password, String channel) {

        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error autenticando: " + e.getMessage());
        }

        Customer customer = customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verificationCodeService.generateAndSendCode(
                customer,
                VerificationChannel.valueOf(channel.toUpperCase()),
                VerificationPurpose.LOGIN
        );

        return "Verification code sent via " + channel;
    }

    @Override
    public AuthResponse verifyCode(String email,
                                   String code,
                                   HttpServletResponse response) {

        Customer customer = customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new RuntimeException("Invalid code");
        }

        return createAuthResponse(customer, response, true);
    }

    @Override
    public AuthResponse authenticateWithGoogle(String credential,
                                               HttpServletResponse response) {

        if (googleClientId == null || googleClientId.isBlank()) {
            throw new RuntimeException("Google login is not configured");
        }

        GoogleTokenInfo tokenInfo = restTemplate.getForObject(
                GOOGLE_TOKEN_INFO_URL,
                GoogleTokenInfo.class,
                credential
        );

        if (tokenInfo == null || isBlank(tokenInfo.getEmail())) {
            throw new RuntimeException("Invalid Google credential");
        }

        if (!googleClientId.equals(tokenInfo.getAud())) {
            throw new RuntimeException("Google credential audience mismatch");
        }

        if (!"true".equalsIgnoreCase(tokenInfo.getEmailVerified())) {
            throw new RuntimeException("Google email is not verified");
        }

        Customer customer = customerRepository
                .findByCustomerEmail(tokenInfo.getEmail().trim().toLowerCase())
                .map(existing -> mergeGoogleProfile(existing, tokenInfo))
                .orElseGet(() -> createGoogleCustomer(tokenInfo));

        return createAuthResponse(customer, response, true);
    }

    @Override
    public AuthResponse refresh(HttpServletRequest request) {

        if (request.getCookies() == null) {
            throw new RuntimeException("No refresh token");
        }

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || jwtService.isTokenExpired(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

        String email = jwtService.extractUsername(refreshToken);

        Customer customer = customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return createAuthResponse(customer, null, false);
    }

    @Override
    public void logout(HttpServletResponse response) {

        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
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

        return changed ? customerRepository.save(customer) : customer;
    }

    private Customer createGoogleCustomer(GoogleTokenInfo tokenInfo) {
        Customer customer = new Customer();
        customer.setCustomerEmail(tokenInfo.getEmail().trim().toLowerCase());
        customer.setCustomerName(defaultIfBlank(tokenInfo.getGivenName(), tokenInfo.getName()));
        customer.setCustomerLastName(defaultIfBlank(tokenInfo.getFamilyName(), ""));
        customer.setCustomerPhoneNumber("");
        customer.setCustomerPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        customer.setRole(resolveCustomerRole());

        return customerRepository.save(customer);
    }

    private Role resolveCustomerRole() {
        return roleRepository.findByRoleName("ROLE_CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol no encontrado"));
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
                    .secure(false)
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
}
