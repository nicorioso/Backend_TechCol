package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.dto.AuthResponse;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationCodeService verificationCodeService;

    public AuthenticationServiceImpl(CustomerRepository customerRepository,
                                     JwtService jwtService,
                                     AuthenticationManager authManager,
                                     VerificationCodeService verificationCodeService) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.verificationCodeService = verificationCodeService;
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
                VerificationChannel.valueOf(channel.toUpperCase())
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

        String role = customer.getRole().getRoleName();

        String accessToken = jwtService.generateAccessToken(email, role);
        String refreshToken = jwtService.generateRefreshToken(email);

        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // TRUE en producción HTTPS
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthResponse(accessToken);
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

        String role = customer.getRole().getRoleName();

        String newAccessToken =
                jwtService.generateAccessToken(email, role);

        return new AuthResponse(newAccessToken);
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
}
