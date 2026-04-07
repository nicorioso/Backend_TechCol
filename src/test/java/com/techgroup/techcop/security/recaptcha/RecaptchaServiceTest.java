package com.techgroup.techcop.security.recaptcha;

import com.techgroup.techcop.model.dto.RecaptchaVerificationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RecaptchaServiceTest {

    private RestTemplate restTemplate;
    private HttpServletRequest request;
    private RecaptchaService recaptchaService;

    @BeforeEach
    void setUp() {
        restTemplate = mock(RestTemplate.class);
        request = mock(HttpServletRequest.class);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        recaptchaService = new RecaptchaService(restTemplate, "secret-key");
    }

    @Test
    void shouldSendTokenAndSecretToGoogle() {
        RecaptchaVerificationResponse response = new RecaptchaVerificationResponse();
        response.setSuccess(true);
        response.setHostname("localhost");

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaVerificationResponse.class)))
                .thenReturn(response);

        recaptchaService.validate("captcha-token", request, "login");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForObject(
                eq("https://www.google.com/recaptcha/api/siteverify"),
                entityCaptor.capture(),
                eq(RecaptchaVerificationResponse.class)
        );

        HttpEntity captured = entityCaptor.getValue();
        MultiValueMap<String, String> body = (MultiValueMap<String, String>) captured.getBody();
        assertEquals("secret-key", body.getFirst("secret"));
        assertEquals("captcha-token", body.getFirst("response"));
        assertEquals("127.0.0.1", body.getFirst("remoteip"));
    }

    @Test
    void shouldReturnClearMessageForTimeoutOrDuplicate() {
        RecaptchaVerificationResponse response = new RecaptchaVerificationResponse();
        response.setSuccess(false);
        response.setErrorCodes(List.of("timeout-or-duplicate"));

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaVerificationResponse.class)))
                .thenReturn(response);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recaptchaService.validate("used-token", request, "login")
        );

        assertEquals(403, exception.getStatusCode().value());
        assertEquals("El reCAPTCHA expiro o ya fue usado. Marca de nuevo 'No soy un robot'.", exception.getReason());
    }

    @Test
    void shouldReturnServerErrorForInvalidSecret() {
        RecaptchaVerificationResponse response = new RecaptchaVerificationResponse();
        response.setSuccess(false);
        response.setErrorCodes(List.of("invalid-input-secret"));

        when(restTemplate.postForObject(any(String.class), any(HttpEntity.class), eq(RecaptchaVerificationResponse.class)))
                .thenReturn(response);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> recaptchaService.validate("captcha-token", request, "login")
        );

        assertEquals(500, exception.getStatusCode().value());
        assertEquals("reCAPTCHA no esta configurado correctamente en el servidor.", exception.getReason());
    }
}
