package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthenticatedPasswordChangeConfirmRequest {

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La nueva contrasena debe tener entre 8 y 72 caracteres")
    private String newPassword;

    public AuthenticatedPasswordChangeConfirmRequest() {
    }

    public AuthenticatedPasswordChangeConfirmRequest(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
