package com.techgroup.techcop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.security.config.SecurityConfig;
import com.techgroup.techcop.security.jwt.JwtAuthEntryPoint;
import com.techgroup.techcop.security.jwt.JwtAuthenticationFilter;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.model.CustomUserDetailsService;
import com.techgroup.techcop.security.ratelimit.RateLimitingFilter;
import com.techgroup.techcop.security.recaptcha.RecaptchaService;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.auth.ChangePasswordService;
import com.techgroup.techcop.service.auth.RegistrationService;
import com.techgroup.techcop.support.TestCorsConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class AuthControllerRecaptchaTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private RegistrationService registrationService;

    @MockBean
    private ChangePasswordService changePasswordService;

    @MockBean
    private RecaptchaService recaptchaService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldForwardRecaptchaTokenOnLoginJson() throws Exception {
        when(authenticationService.login("user@test.com", "Secret123!", "EMAIL"))
                .thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "password": "Secret123!",
                                  "channel": "EMAIL",
                                  "recaptchaToken": "login-captcha"
                                }
                                """))
                .andExpect(status().isOk());

        verify(recaptchaService).validate(eq("login-captcha"), any(), eq("login"));
        verify(authenticationService).login("user@test.com", "Secret123!", "EMAIL");
    }

    @Test
    void shouldForwardIdentifierAndChannelOnSmsLoginJson() throws Exception {
        when(authenticationService.login("+573001112233", "Secret123!", "SMS"))
                .thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "+573001112233",
                                  "password": "Secret123!",
                                  "channel": "SMS",
                                  "recaptchaToken": "login-sms-captcha"
                                }
                                """))
                .andExpect(status().isOk());

        verify(recaptchaService).validate(eq("login-sms-captcha"), any(), eq("login"));
        verify(authenticationService).login("+573001112233", "Secret123!", "SMS");
    }

    @Test
    void shouldReadRecaptchaAliasOnLoginJson() throws Exception {
        when(authenticationService.login("user@test.com", "Secret123!", "EMAIL"))
                .thenReturn(null);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "user@test.com",
                                  "password": "Secret123!",
                                  "channel": "EMAIL",
                                  "g-recaptcha-response": "login-alias-token"
                                }
                                """))
                .andExpect(status().isOk());

        verify(recaptchaService).validate(eq("login-alias-token"), any(), eq("login"));
    }

    @Test
    void shouldForwardRecaptchaTokenOnRegister() throws Exception {
        when(registrationService.registerRequest(any())).thenReturn("Verification code sent");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerName": "Test",
                                  "customerLastName": "User",
                                  "customerEmail": "user@test.com",
                                  "customerPassword": "Secret123!",
                                  "customerPhoneNumber": "+573001112233",
                                  "channel": "SMS",
                                  "g-recaptcha-response": "register-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code sent"));

        verify(recaptchaService).validate(eq("register-token"), any(), eq("register"));
    }

    @Test
    void shouldForwardRecaptchaTokenOnRegisterResend() throws Exception {
        when(registrationService.resendRegisterCode("+573001112233", "SMS"))
                .thenReturn("Verification code sent");

        mockMvc.perform(post("/auth/register/resend-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "identifier": "+573001112233",
                                  "channel": "SMS",
                                  "recaptchaToken": "register-resend-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code sent"));

        verify(recaptchaService).validate(eq("register-resend-token"), any(), eq("register-resend"));
        verify(registrationService).resendRegisterCode("+573001112233", "SMS");
    }

    @Test
    void shouldForwardRecaptchaTokenOnForgotPassword() throws Exception {
        when(changePasswordService.requestPasswordRecovery("user@test.com", "EMAIL"))
                .thenReturn("Verification code sent");

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PasswordRecoveryRequestBody(
                                "user@test.com",
                                "EMAIL",
                                "captcha-token"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().string("Verification code sent"));

        verify(recaptchaService).validate(eq("captcha-token"), any(), eq("password-recovery-request"));
        verify(changePasswordService).requestPasswordRecovery("user@test.com", "EMAIL");
    }

    @Test
    void shouldReturnForbiddenWhenForgotPasswordRecaptchaFails() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Captcha invalido o expirado."))
                .when(recaptchaService)
                .validate(eq("bad-token"), any(), eq("password-recovery-request"));

        mockMvc.perform(post("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PasswordRecoveryRequestBody(
                                "user@test.com",
                                "EMAIL",
                                "bad-token"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Captcha invalido o expirado."));
    }

    @Test
    void shouldForwardRecaptchaTokenOnResetPassword() throws Exception {
        when(changePasswordService.resetPasswordByRecovery("user@test.com", "EMAIL", "NewPass123!"))
                .thenReturn("Change password successful");

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PasswordRecoveryResetBody(
                                "user@test.com",
                                "EMAIL",
                                "NewPass123!",
                                "reset-captcha"
                        ))))
                .andExpect(status().isOk())
                .andExpect(content().string("Change password successful"));

        verify(recaptchaService).validate(eq("reset-captcha"), any(), eq("password-recovery-reset"));
        verify(changePasswordService).resetPasswordByRecovery("user@test.com", "EMAIL", "NewPass123!");
    }

    @Test
    void shouldReturnForbiddenWhenResetPasswordRecaptchaFails() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Captcha invalido o expirado."))
                .when(recaptchaService)
                .validate(eq("reset-bad-token"), any(), eq("password-recovery-reset"));

        mockMvc.perform(post("/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(new PasswordRecoveryResetBody(
                                "user@test.com",
                                "EMAIL",
                                "NewPass123!",
                                "reset-bad-token"
                        ))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Captcha invalido o expirado."));
    }

    @Test
    void shouldAllowCredentialedCorsOnAuthLoginPreflight() throws Exception {
        mockMvc.perform(options("/auth/login")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    private record PasswordRecoveryRequestBody(
            String identifier,
            String channel,
            String recaptchaToken
    ) {
    }

    private record PasswordRecoveryResetBody(
            String identifier,
            String channel,
            String newPassword,
            String recaptchaToken
    ) {
    }
}
