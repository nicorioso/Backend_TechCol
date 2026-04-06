package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String email;

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

    public LoginRequest(String email, String password, String channel) {
        this.email = email;
        this.password = password;
        this.channel = channel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
