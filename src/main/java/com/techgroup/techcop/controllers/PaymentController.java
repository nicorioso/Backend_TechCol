package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.PaymentCallbackResponse;
import com.techgroup.techcop.service.payment.PaymentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

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
        try {
            return ResponseEntity.ok(paymentService.createPayPalOrder());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PostMapping("/capture/{paypalOrderId}")
    public ResponseEntity<?> capture(
            @PathVariable String paypalOrderId) {
        try {
            paymentService.captureOrder(paypalOrderId);
            return ResponseEntity.ok(Map.of("message", "Payment successful and order created"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/return")
    public ResponseEntity<?> paypalReturn(
            @RequestParam("token") String token,
            @RequestParam(value = "PayerID", required = false) String payerId) {
        return buildCallbackResponse(paymentService.handlePaypalReturn(token, payerId));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> paypalCancel(
            @RequestParam(value = "token", required = false) String token) {
        return buildCallbackResponse(paymentService.handlePaypalCancel(token));
    }

    private ResponseEntity<?> buildCallbackResponse(PaymentCallbackResponse response) {
        if (response.getRedirectUrl() != null && !response.getRedirectUrl().isBlank()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, response.getRedirectUrl())
                    .build();
        }
        return ResponseEntity.ok(response);
    }
}
