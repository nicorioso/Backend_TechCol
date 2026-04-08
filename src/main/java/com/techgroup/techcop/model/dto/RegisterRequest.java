package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @JsonAlias("email")
    @NotBlank(message = "El identificador es obligatorio")
    @Size(max = 255, message = "El identificador no puede superar los 255 caracteres")
    private String identifier;

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El codigo debe tener 6 digitos")
    private String code;

    @Pattern(
            regexp = "(?i)^(EMAIL|SMS)?$",
            message = "El canal debe ser EMAIL o SMS"
    )
    private String channel;

    public RegisterRequest(){}

    public RegisterRequest(String code, String identifier) {
        this.code = code;
        this.identifier = identifier;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
