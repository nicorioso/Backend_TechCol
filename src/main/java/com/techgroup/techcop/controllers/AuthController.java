package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.AuthResponse;
import com.techgroup.techcop.model.dto.LoginRequest;
import com.techgroup.techcop.model.dto.VerifyCodeRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;

    public AuthController(AuthenticationService authenticationService,
                          RegistrationService registrationService) {
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Customer customer) {
        return ResponseEntity.ok(
                registrationService.register(customer)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authenticationService.login(
                        request.getEmail(),
                        request.getPassword()
                )
        );
    }

    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(
            @RequestBody VerifyCodeRequest request,
            HttpServletResponse response) {

        return ResponseEntity.ok(
                authenticationService.verifyCode(
                        request.getEmail(),
                        request.getCode(),
                        response
                )
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request) {

        return ResponseEntity.ok(
                authenticationService.refresh(request)
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        authenticationService.logout(response);
        return ResponseEntity.ok("Logged out");
    }
}
