package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.MessageResponse;
import com.techgroup.techcop.model.dto.PayPalOrderResponse;
import com.techgroup.techcop.model.dto.PaymentCallbackResponse;
import com.techgroup.techcop.service.payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/paypal")
@Tag(name = "Payments", description = "Integracion PayPal para pagos y callbacks")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Crear una orden en PayPal")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PostMapping("/create-order")
    public ResponseEntity<PayPalOrderResponse> createOrder() {
        return ResponseEntity.ok(paymentService.createPayPalOrder());
    }

    @Operation(summary = "Capturar una orden de PayPal")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @PostMapping("/capture/{paypalOrderId}")
    public ResponseEntity<MessageResponse> capture(
            @PathVariable @NotBlank(message = "El id de PayPal es obligatorio") String paypalOrderId) {
        paymentService.captureOrder(paypalOrderId);
        return ResponseEntity.ok(new MessageResponse("Payment successful and order created"));
    }

    @Operation(summary = "Procesar el callback de retorno de PayPal")
    @GetMapping("/return")
    public ResponseEntity<?> paypalReturn(
            @RequestParam("token") @NotBlank(message = "El token de PayPal es obligatorio") String token,
            @RequestParam(value = "PayerID", required = false) String payerId) {
        return buildCallbackResponse(paymentService.handlePaypalReturn(token, payerId));
    }

    @Operation(summary = "Procesar el callback de cancelacion de PayPal")
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
