package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class AuthenticatedPasswordChangeRequest {

    @NotBlank(message = "El canal es obligatorio")
    @Pattern(regexp = "(?i)^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    private String channel;

    public AuthenticatedPasswordChangeRequest() {
    }

    public AuthenticatedPasswordChangeRequest(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
