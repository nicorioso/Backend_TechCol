package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.*;
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

    @GetMapping("/google/client-config")
    public ResponseEntity<GoogleClientConfigResponse> googleClientConfig() {
        return ResponseEntity.ok(
                new GoogleClientConfigResponse(authenticationService.getGoogleClientId())
        );
    }

    @PostMapping("/account-exists")
    public ResponseEntity<AccountExistsResponse> accountExists(@RequestBody AccountExistsRequest request) {
        return ResponseEntity.ok(
                new AccountExistsResponse(
                        authenticationService.accountExists(request.getEmail())
                )
        );
    }

    @PostMapping("/verifyRegister")
    public ResponseEntity<?> verifiRegister(@RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(
                registrationService.verifyRegister(
                        registerRequest.getEmail(),
                        registerRequest.getCode()
                )
        );
    }

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

    @PostMapping("/password-recovery/request")
    public ResponseEntity<?> requestPasswordRecovery(@RequestBody PasswordRecoveryRequest request) {
        return ResponseEntity.ok(
                changePasswordService.requestPasswordRecovery(
                        request.getIdentifier(),
                        request.getChannel()
                )
        );
    }

    @PostMapping("/password-recovery/verify")
    public ResponseEntity<?> verifyPasswordRecovery(@RequestBody PasswordRecoveryVerifyRequest request) {
        return ResponseEntity.ok(
                changePasswordService.verifyPasswordRecoveryCode(
                        request.getIdentifier(),
                        request.getChannel(),
                        request.getCode()
                )
        );
    }

    @PostMapping("/password-recovery/reset")
    public ResponseEntity<?> resetPasswordRecovery(@RequestBody PasswordRecoveryResetRequest request) {
        return ResponseEntity.ok(
                changePasswordService.resetPasswordByRecovery(
                        request.getIdentifier(),
                        request.getChannel(),
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
