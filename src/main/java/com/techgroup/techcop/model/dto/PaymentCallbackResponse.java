package com.techgroup.techcop.model.dto;

public class PaymentCallbackResponse {

    private String status;
    private String paypalOrderId;
    private String payerId;
    private String message;
    private String redirectUrl;

    public PaymentCallbackResponse() {
    }

    public PaymentCallbackResponse(String status, String paypalOrderId, String payerId, String message, String redirectUrl) {
        this.status = status;
        this.paypalOrderId = paypalOrderId;
        this.payerId = payerId;
        this.message = message;
        this.redirectUrl = redirectUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String getPayerId() {
        return payerId;
    }

    public void setPayerId(String payerId) {
        this.payerId = payerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
