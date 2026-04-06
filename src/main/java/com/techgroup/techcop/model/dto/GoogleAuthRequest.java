package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "La credencial de Google es obligatoria")
    private String credential;

    public GoogleAuthRequest() {
    }

    public GoogleAuthRequest(String credential) {
        this.credential = credential;
    }

    public String getCredential() {
        return credential;
    }

    public void setCredential(String credential) {
        this.credential = credential;
    }
}
