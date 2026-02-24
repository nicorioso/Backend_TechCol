package com.techgroup.techcop.service.payment;

public interface PaymentService {
    String createPayPalOrder();
    void captureOrder(String paypalOrderId);
}
