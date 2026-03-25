package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.AuthResponse;
import com.techgroup.techcop.model.dto.ChangePasswordRequest;
import com.techgroup.techcop.model.dto.GoogleAuthRequest;
import com.techgroup.techcop.model.dto.LoginRequest;
import com.techgroup.techcop.model.dto.VerifyCodeRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.ChangePasswordService;
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
    private final ChangePasswordService changePasswordService;

    public AuthController(AuthenticationService authenticationService,
                          RegistrationService registrationService,
                          ChangePasswordService changePasswordService) {
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
        this.changePasswordService = changePasswordService;
    }

    @PostMapping("/registerRequest")
    public ResponseEntity<?> registerRequest(@RequestBody Customer customer) {
        return ResponseEntity.ok(
                registrationService.registerRequest(customer)
        );
    }

    @PostMapping("/verifyRegister")
    public ResponseEntity<?> verifiRegister(@RequestBody )

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                authenticationService.login(
                        request.getEmail(),
                        request.getPassword(),
                        request.getChannel()
                )
        );
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(
                authenticationService.authenticateWithGoogle(
                        request.getCredential(),
                        response
                )
        );
    }

    @PostMapping("/changePasswordAuthen")
    public ResponseEntity<?> changePasswordAuthenticate (@RequestBody LoginRequest login) {
        return ResponseEntity.ok(
                changePasswordService.changePasswordAuthenticate(
                        login.getEmail(),
                        login.getPassword(),
                        login.getChannel()
                )
        );
    }

    @PostMapping("/changePasswordVerifiCode")
    public ResponseEntity<?> changePasswordVerifiCode (@RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(
                changePasswordService.changePasswordVerifiCode(
                        request.getEmail(),
                        request.getCode()
                )
        );
    }

    @PostMapping("changePassword")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(
                changePasswordService.changePassword(
                        request.getEmail(),
                        request.getNewPassword()
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
