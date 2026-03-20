package com.techgroup.techcop.model.dto;

public class AuthenticatedUserDto {

    private Integer customerId;
    private String customerName;
    private String customerLastName;
    private String customerEmail;
    private String role;

    public AuthenticatedUserDto(Integer customerId, String customerName, String customerLastName, String customerEmail, String role) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerLastName = customerLastName;
        this.customerEmail = customerEmail;
        this.role = role;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getRole() {
        return role;
    }
}
