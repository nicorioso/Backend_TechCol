package com.techgroup.techcop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customerId")
    private Integer customer_id;

    @Column(name = "customerName")
    private String name;

    @Column(name = "customerLastName")
    private String last_name;

    @Column(name = "customerEmail")
    private String email;

    @Column(name = "customerPassword")
    private String user_password;

    @Column(name = "customerPhoneNumber")
    private String phone_number;

    @Column(name = "roleId")
    private Integer role_id;

    public Customer() {

    }

    public Customer(Integer customer_id, String name, String last_name, String email, String user_password
            , String phone_number, Integer role_id) {
        this.customer_id = customer_id;
        this.name = name;
        this.last_name = last_name;
        this.email = email;
        this.user_password = user_password;
        this.phone_number = phone_number;
        this.role_id = role_id;
    }

    public Integer getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Integer customer_id) {
        this.customer_id = customer_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_password() {
        return user_password;
    }

    public void setUser_password(String user_password) {
        this.user_password = user_password;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Integer getRole_id() {
        return role_id;
    }

    public void setRole_id(Integer role_id) {
        this.role_id = role_id;
    }
}
