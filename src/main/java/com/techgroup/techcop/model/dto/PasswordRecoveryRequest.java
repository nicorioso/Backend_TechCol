package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordRecoveryRequest {

    @NotBlank(message = "El identificador es obligatorio")
    @Size(max = 255, message = "El identificador no puede superar los 255 caracteres")
    private String identifier;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "(?i)^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    private String channel;

    @JsonAlias("g-recaptcha-response")
    @NotBlank(message = "Debes completar el reCAPTCHA")
    private String recaptchaToken;

    public PasswordRecoveryRequest() {
    }

    public PasswordRecoveryRequest(String identifier, String channel, String recaptchaToken) {
        this.identifier = identifier;
        this.channel = channel;
        this.recaptchaToken = recaptchaToken;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getRecaptchaToken() {
        return recaptchaToken;
    }

    public void setRecaptchaToken(String recaptchaToken) {
        this.recaptchaToken = recaptchaToken;
    }
}
