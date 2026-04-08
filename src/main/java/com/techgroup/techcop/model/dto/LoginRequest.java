package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @JsonAlias("email")
    @NotBlank(message = "El identificador es obligatorio")
    @Size(max = 255, message = "El identificador no puede superar los 255 caracteres")
    private String identifier;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(max = 72, message = "La contrasena no puede superar los 72 caracteres")
    private String password;

    @Pattern(
            regexp = "(?i)^(EMAIL|SMS)?$",
            message = "El canal debe ser EMAIL o SMS"
    )
    private String channel;

    @JsonAlias("g-recaptcha-response")
    private String recaptchaToken;

    public LoginRequest() {
    }

    public LoginRequest(String identifier, String password, String channel) {
        this.identifier = identifier;
        this.password = password;
        this.channel = channel;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getEmail() {
        return identifier;
    }

    public void setEmail(String email) {
        this.identifier = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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
