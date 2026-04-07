package com.techgroup.techcop.security.recaptcha;

import com.techgroup.techcop.model.dto.RecaptchaVerificationResponse;
import com.techgroup.techcop.security.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);
    private static final String RECAPTCHA_VERIFY_URL =
            "https://www.google.com/recaptcha/api/siteverify";

    private final RestTemplate restTemplate;
    private final String secretKey;

    public RecaptchaService(RestTemplate restTemplate,
                            @Value("${recaptcha.secret:}") String secretKey) {
        this.restTemplate = restTemplate;
        this.secretKey = secretKey;
    }

    public void validate(String token, HttpServletRequest request, String flowName) {
        String clientIp = ClientIpResolver.resolve(request);
        String configuredSecretKey = hasText(secretKey) ? secretKey.trim() : "";

        if (!hasText(configuredSecretKey)) {
            log.error("reCAPTCHA no configurado. Falta RECAPTCHA_SECRET_KEY para el flujo {}", flowName);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "reCAPTCHA no esta configurado en el servidor."
            );
        }

        if (!hasText(token)) {
            log.warn("reCAPTCHA ausente para el flujo {} desde IP {}", flowName, clientIp);
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Debes completar el reCAPTCHA."
            );
        }

        log.info(
                "Validando reCAPTCHA para flujo {} desde IP {}. tokenPreview={}",
                flowName,
                clientIp,
                tokenPreview(token)
        );

        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
        requestBody.add("secret", configuredSecretKey);
        requestBody.add("response", token.trim());
        requestBody.add("remoteip", clientIp);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            RecaptchaVerificationResponse verification = restTemplate.postForObject(
                    RECAPTCHA_VERIFY_URL,
                    new HttpEntity<>(requestBody, headers),
                    RecaptchaVerificationResponse.class
            );

            if (verification == null) {
                log.error("Google reCAPTCHA respondio vacio para el flujo {} desde IP {}", flowName, clientIp);
                throw new ResponseStatusException(
                        HttpStatus.SERVICE_UNAVAILABLE,
                        "No fue posible validar reCAPTCHA en este momento."
                );
            }

            if (!verification.isSuccess()) {
                log.warn(
                        "reCAPTCHA invalido para el flujo {} desde IP {}. hostname={}, errores={}",
                        flowName,
                        clientIp,
                        verification.getHostname(),
                        verification.getErrorCodes()
                );
                throw mapVerificationFailure(verification);
            }

            log.info(
                    "reCAPTCHA valido para flujo {} desde IP {}. hostname={}",
                    flowName,
                    clientIp,
                    verification.getHostname()
            );
        } catch (RestClientException ex) {
            log.error("No fue posible validar reCAPTCHA para el flujo {} desde IP {}", flowName, clientIp, ex);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "No fue posible validar reCAPTCHA en este momento."
            );
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String tokenPreview(String token) {
        if (!hasText(token)) {
            return "missing";
        }

        String trimmed = token.trim();
        return trimmed.substring(0, Math.min(12, trimmed.length())) + "...";
    }

    private ResponseStatusException mapVerificationFailure(RecaptchaVerificationResponse verification) {
        List<String> errorCodes = verification.getErrorCodes();

        if (errorCodes == null || errorCodes.isEmpty()) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Captcha invalido o expirado. Marca de nuevo 'No soy un robot'."
            );
        }

        if (errorCodes.contains("timeout-or-duplicate")) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El reCAPTCHA expiro o ya fue usado. Marca de nuevo 'No soy un robot'."
            );
        }

        if (errorCodes.contains("missing-input-response")) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Debes completar el reCAPTCHA."
            );
        }

        if (errorCodes.contains("invalid-input-response")) {
            return new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El token de reCAPTCHA no es valido. Marca de nuevo 'No soy un robot'."
            );
        }

        if (errorCodes.contains("missing-input-secret") || errorCodes.contains("invalid-input-secret")) {
            return new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "reCAPTCHA no esta configurado correctamente en el servidor."
            );
        }

        if (errorCodes.contains("bad-request")) {
            return new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Google reCAPTCHA rechazo la validacion. Intenta de nuevo."
            );
        }

        return new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Captcha invalido o expirado. Marca de nuevo 'No soy un robot'."
        );
    }
}
