package com.techgroup.techcop.controllers;

import com.techgroup.techcop.service.payment.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/paypal")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder() {
        return ResponseEntity.ok(
                paymentService.createPayPalOrder()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PostMapping("/capture/{paypalOrderId}")
    public ResponseEntity<?> capture(
            @PathVariable String paypalOrderId) {

        paymentService.captureOrder(paypalOrderId);
        return ResponseEntity.ok("Payment successful and order created");
    }
}