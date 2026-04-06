package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VerifyCodeRequest {

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String email;

    @NotBlank(message = "El codigo es obligatorio")
    @Pattern(regexp = "^\\d{6}$", message = "El codigo debe tener 6 digitos")
    private String code;

    public VerifyCodeRequest () {}

    public VerifyCodeRequest(String email, String code) {
        this.email = email;
        this.code = code;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
