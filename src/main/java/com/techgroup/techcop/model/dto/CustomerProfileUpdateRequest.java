package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomerProfileUpdateRequest {

    @Size(max = 80, message = "El nombre no puede superar los 80 caracteres")
    private String customerName;

    @Size(max = 80, message = "El apellido no puede superar los 80 caracteres")
    private String customerLastName;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato valido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String customerEmail;

    @Pattern(
            regexp = "^$|^[0-9+()\\-\\s]{7,20}$",
            message = "El telefono debe tener entre 7 y 20 caracteres validos"
    )
    private String customerPhoneNumber;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public void setCustomerPhoneNumber(String customerPhoneNumber) {
        this.customerPhoneNumber = customerPhoneNumber;
    }

    @JsonSetter("customerPassword")
    public void rejectCustomerPassword(String ignored) {
        throw new IllegalArgumentException("Password must be updated through /auth/change-password");
    }

    @JsonSetter("password")
    public void rejectPassword(String ignored) {
        throw new IllegalArgumentException("Password must be updated through /auth/change-password");
    }
}
