package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordRecoveryVerifyRequest {

    @NotBlank(message = "El identificador es obligatorio")
    @Size(max = 255, message = "El identificador no puede superar los 255 caracteres")
    private String identifier;

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "(?i)^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    private String channel;

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El codigo debe tener 6 digitos")
    private String code;

    public PasswordRecoveryVerifyRequest() {
    }

    public PasswordRecoveryVerifyRequest(String identifier, String channel, String code) {
        this.identifier = identifier;
        this.channel = channel;
        this.code = code;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
