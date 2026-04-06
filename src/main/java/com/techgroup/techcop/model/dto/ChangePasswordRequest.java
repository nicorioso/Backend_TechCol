package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ChangePasswordRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String email;

    @NotBlank(message = "La nueva contrasena es obligatoria")
    @Size(min = 8, max = 72, message = "La nueva contrasena debe tener entre 8 y 72 caracteres")
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String email, String newPassword) {
        this.email = email;
        this.newPassword = newPassword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
