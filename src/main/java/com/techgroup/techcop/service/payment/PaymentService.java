package com.techgroup.techcop.service.payment;

import com.techgroup.techcop.model.dto.PaymentCallbackResponse;
import com.techgroup.techcop.model.dto.PayPalOrderResponse;

public interface PaymentService {
    PayPalOrderResponse createPayPalOrder();
    void captureOrder(String paypalOrderId);
    PaymentCallbackResponse handlePaypalReturn(String token, String payerId);
    PaymentCallbackResponse handlePaypalCancel(String token);
}
