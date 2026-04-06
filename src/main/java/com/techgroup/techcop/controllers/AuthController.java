package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.*;
import com.techgroup.techcop.security.recaptcha.RecaptchaService;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.ChangePasswordService;
import com.techgroup.techcop.service.auth.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints publicos y autenticados para registro, login y gestion de credenciales")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationService authenticationService;
    private final RegistrationService registrationService;
    private final ChangePasswordService changePasswordService;
    private final RecaptchaService recaptchaService;

    public AuthController(AuthenticationService authenticationService,
                          RegistrationService registrationService,
                          ChangePasswordService changePasswordService,
                          RecaptchaService recaptchaService) {
        this.authenticationService = authenticationService;
        this.registrationService = registrationService;
        this.changePasswordService = changePasswordService;
        this.recaptchaService = recaptchaService;
    }

    @Operation(summary = "Iniciar registro de usuario")
    @PostMapping({"/register", "/registerRequest"})
    public ResponseEntity<?> registerRequest(@Valid @RequestBody RegisterCustomerRequest request,
                                             HttpServletRequest servletRequest) {
        recaptchaService.validate(request.getRecaptchaToken(), servletRequest, "register");
        return ResponseEntity.ok(
                registrationService.registerRequest(request)
        );
    }

    @Operation(summary = "Obtener la configuracion publica de Google OAuth")
    @GetMapping("/google/client-config")
    public ResponseEntity<GoogleClientConfigResponse> googleClientConfig() {
        return ResponseEntity.ok(
                new GoogleClientConfigResponse(authenticationService.getGoogleClientId())
        );
    }

    @Operation(summary = "Verificar si ya existe una cuenta para un correo")
    @PostMapping("/account-exists")
    public ResponseEntity<AccountExistsResponse> accountExists(@Valid @RequestBody AccountExistsRequest request) {
        return ResponseEntity.ok(
                new AccountExistsResponse(
                        authenticationService.accountExists(request.getEmail())
                )
        );
    }

    @Operation(summary = "Confirmar el codigo de verificacion del registro")
    @PostMapping("/verifyRegister")
    public ResponseEntity<?> verifiRegister(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(
                registrationService.verifyRegister(
                        registerRequest.getEmail(),
                        registerRequest.getCode()
                )
        );
    }

    @Operation(summary = "Solicitar login con correo, contrasena y reCAPTCHA")
    @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> loginJson(@Valid @RequestBody LoginRequest request,
                                       HttpServletRequest servletRequest) {
        return handleLogin(request, servletRequest);
    }

    @Operation(summary = "Solicitar login con formulario y reCAPTCHA")
    @PostMapping(value = "/login", consumes = {
            MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            MediaType.MULTIPART_FORM_DATA_VALUE
    })
    public ResponseEntity<?> loginForm(@Valid @ModelAttribute LoginRequest request,
                                       HttpServletRequest servletRequest) {
        return handleLogin(request, servletRequest);
    }

    @Operation(summary = "Autenticar mediante Google OAuth")
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleAuthRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(
                authenticationService.authenticateWithGoogle(
                        request.getCredential(),
                        response
                )
        );
    }

    @Operation(summary = "Validar credenciales antes de iniciar el cambio de contrasena")
    @PostMapping("/changePasswordAuthen")
    public ResponseEntity<?> changePasswordAuthenticate (@Valid @RequestBody LoginRequest login) {
        String channel = normalize(login.getChannel());
        return ResponseEntity.ok(
                changePasswordService.changePasswordAuthenticate(
                        login.getEmail(),
                        login.getPassword(),
                        channel.isEmpty() ? "EMAIL" : channel
                )
        );
    }

    @Operation(summary = "Verificar el codigo para habilitar el cambio de contrasena")
    @PostMapping("/changePasswordVerifiCode")
    public ResponseEntity<?> changePasswordVerifiCode (@Valid @RequestBody VerifyCodeRequest request) {
        return ResponseEntity.ok(
                changePasswordService.changePasswordVerifiCode(
                        request.getEmail(),
                        request.getCode()
                )
        );
    }

    @Operation(summary = "Cambiar la contrasena de un usuario verificado")
    @PostMapping({"/changePassword", "/change-password"})
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request){
        return ResponseEntity.ok(
                changePasswordService.changePassword(
                        request.getEmail(),
                        request.getNewPassword()
                )
        );
    }

    @Operation(summary = "Solicitar recuperacion de contrasena")
    @PostMapping({"/password-recovery/request", "/forgot-password"})
    public ResponseEntity<?> requestPasswordRecovery(@Valid @RequestBody PasswordRecoveryRequest request,
                                                     HttpServletRequest servletRequest) {
        recaptchaService.validate(request.getRecaptchaToken(), servletRequest, "password-recovery-request");
        return ResponseEntity.ok(
                changePasswordService.requestPasswordRecovery(
                        request.getIdentifier(),
                        request.getChannel()
                )
        );
    }

    @Operation(summary = "Verificar el codigo de recuperacion de contrasena")
    @PostMapping("/password-recovery/verify")
    public ResponseEntity<?> verifyPasswordRecovery(@Valid @RequestBody PasswordRecoveryVerifyRequest request) {
        return ResponseEntity.ok(
                changePasswordService.verifyPasswordRecoveryCode(
                        request.getIdentifier(),
                        request.getChannel(),
                        request.getCode()
                )
        );
    }

    @Operation(summary = "Restablecer contrasena con recuperacion previa verificada")
    @PostMapping({"/password-recovery/reset", "/reset-password"})
    public ResponseEntity<?> resetPasswordRecovery(@Valid @RequestBody PasswordRecoveryResetRequest request,
                                                   HttpServletRequest servletRequest) {
        recaptchaService.validate(request.getRecaptchaToken(), servletRequest, "password-recovery-reset");
        return ResponseEntity.ok(
                changePasswordService.resetPasswordByRecovery(
                        request.getIdentifier(),
                        request.getChannel(),
                        request.getNewPassword()
                )
        );
    }

    @Operation(summary = "Confirmar codigo OTP y emitir tokens JWT")
    @PostMapping("/verify")
    public ResponseEntity<AuthResponse> verify(
            @Valid @RequestBody VerifyCodeRequest request,
            HttpServletResponse response) {

        return ResponseEntity.ok(
                authenticationService.verifyCode(
                        request.getEmail(),
                        request.getCode(),
                        response
                )
        );
    }

    @Operation(summary = "Renovar el access token usando la refresh cookie")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request) {

        return ResponseEntity.ok(
                authenticationService.refresh(request)
        );
    }

    @Operation(summary = "Cerrar sesion e invalidar la refresh cookie")
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletResponse response) {

        authenticationService.logout(response);
        return ResponseEntity.ok(new MessageResponse("Logged out"));
    }

    private ResponseEntity<?> handleLogin(LoginRequest request, HttpServletRequest servletRequest) {
        String recaptchaToken = resolveRecaptchaToken(request, servletRequest);
        String email = normalize(resolveEmail(request, servletRequest));
        String channel = normalize(resolveChannel(request, servletRequest));

        log.info(
                "Intento de login recibido. email={}, channel={}, contentType={}, hasRecaptcha={}, tokenPreview={}",
                email,
                channel,
                servletRequest.getContentType(),
                hasText(recaptchaToken),
                tokenPreview(recaptchaToken)
        );

        try {
            recaptchaService.validate(recaptchaToken, servletRequest, "login");

            return ResponseEntity.ok(
                    authenticationService.login(
                            email,
                            resolvePassword(request, servletRequest),
                            channel.isEmpty() ? "EMAIL" : channel
                    )
            );
        } catch (ResponseStatusException ex) {
            log.warn(
                    "Login rechazado. email={}, status={}, reason={}",
                    email,
                    ex.getStatusCode().value(),
                    ex.getReason()
            );
            throw ex;
        }
    }

    private String resolveRecaptchaToken(LoginRequest request, HttpServletRequest servletRequest) {
        if (request != null && hasText(request.getRecaptchaToken())) {
            return request.getRecaptchaToken();
        }

        String[] candidates = {
                servletRequest.getParameter("recaptchaToken"),
                servletRequest.getParameter("g-recaptcha-response"),
                servletRequest.getHeader("X-Recaptcha-Token")
        };

        for (String candidate : candidates) {
            if (hasText(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private String resolveEmail(LoginRequest request, HttpServletRequest servletRequest) {
        if (request != null && hasText(request.getEmail())) {
            return request.getEmail();
        }
        return servletRequest.getParameter("email");
    }

    private String resolvePassword(LoginRequest request, HttpServletRequest servletRequest) {
        if (request != null && hasText(request.getPassword())) {
            return request.getPassword();
        }
        return servletRequest.getParameter("password");
    }

    private String resolveChannel(LoginRequest request, HttpServletRequest servletRequest) {
        if (request != null && hasText(request.getChannel())) {
            return request.getChannel();
        }
        return servletRequest.getParameter("channel");
    }

    private String tokenPreview(String token) {
        if (!hasText(token)) {
            return "missing";
        }

        String trimmed = token.trim();
        return trimmed.substring(0, Math.min(12, trimmed.length())) + "...";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
