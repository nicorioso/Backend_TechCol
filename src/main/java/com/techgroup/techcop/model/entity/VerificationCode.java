package com.techgroup.techcop.model.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_codes")
public class VerificationCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "expiration_time")
    private LocalDateTime expirationTime;

    @Column(name = "used")
    private boolean used;

    @Column(name = "attempts")
    private int attempts;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    public VerificationCode(){}

    public VerificationCode(Integer id, String code, LocalDateTime expirationTime, boolean used, int attempts, Customer customer) {
        this.id = id;
        this.code = code;
        this.expirationTime = expirationTime;
        this.used = used;
        this.attempts = attempts;
        this.customer = customer;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}

