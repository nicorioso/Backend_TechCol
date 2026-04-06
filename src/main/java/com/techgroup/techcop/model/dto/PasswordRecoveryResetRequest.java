package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordRecoveryResetRequest {

    @NotBlank(message = "El identificador es obligatorio")
    @Size(max = 255, message = "El identificador no puede superar los 255 caracteres")
    private String identifier;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "(?i)^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    private String channel;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La nueva contrasena debe tener entre 8 y 72 caracteres")
    private String newPassword;

    @JsonAlias("g-recaptcha-response")
    @NotBlank(message = "Debes completar el reCAPTCHA")
    private String recaptchaToken;

    public PasswordRecoveryResetRequest() {
    }

    public PasswordRecoveryResetRequest(String identifier, String channel, String newPassword, String recaptchaToken) {
        this.identifier = identifier;
        this.channel = channel;
        this.newPassword = newPassword;
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

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRecaptchaToken() {
        return recaptchaToken;
    }

    public void setRecaptchaToken(String recaptchaToken) {
        this.recaptchaToken = recaptchaToken;
    }
}
