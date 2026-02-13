package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.VerifyCodeRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.dto.LoginRequest;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;

    public AuthController(AuthenticationService authenticationService, RegistrationService registrationService) {
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Customer customer) {
        return ResponseEntity.ok(registrationService.register(customer));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyCodeRequest request) {

        String token = authenticationService
                .verifyCode(request.getEmail(), request.getCode());

        return ResponseEntity.ok(token);
    }


}