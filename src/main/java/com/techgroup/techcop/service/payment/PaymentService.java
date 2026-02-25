package com.techgroup.techcop.service.payment;

import java.util.Map;

public interface PaymentService {
    Map<String, String> createPayPalOrder();
    void captureOrder(String paypalOrderId);
}
