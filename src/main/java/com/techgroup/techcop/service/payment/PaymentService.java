package com.techgroup.techcop.service.payment;

import com.techgroup.techcop.model.dto.PaymentCallbackResponse;

import java.util.Map;

public interface PaymentService {
    Map<String, String> createPayPalOrder();
    void captureOrder(String paypalOrderId);
    PaymentCallbackResponse handlePaypalReturn(String token, String payerId);
    PaymentCallbackResponse handlePaypalCancel(String token);
}
