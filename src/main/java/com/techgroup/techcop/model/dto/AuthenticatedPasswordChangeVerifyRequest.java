package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthenticatedPasswordChangeVerifyRequest {

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "(?i)^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    private String channel;

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El codigo debe tener 6 digitos")
    private String code;

    public AuthenticatedPasswordChangeVerifyRequest() {
    }

    public AuthenticatedPasswordChangeVerifyRequest(String channel, String code) {
        this.channel = channel;
        this.code = code;
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
