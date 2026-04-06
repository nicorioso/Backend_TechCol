package com.techgroup.techcop.model.dto;

public class PayPalOrderResponse {

    private String orderId;
    private String approveUrl;

    public PayPalOrderResponse() {
    }

    public PayPalOrderResponse(String orderId, String approveUrl) {
        this.orderId = orderId;
        this.approveUrl = approveUrl;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getApproveUrl() {
        return approveUrl;
    }

    public void setApproveUrl(String approveUrl) {
        this.approveUrl = approveUrl;
    }
}
