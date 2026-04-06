package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.dto.OrderResponse;
import com.techgroup.techcop.service.order.OrderService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/order")
@Tag(name = "Orders", description = "Consulta y eliminacion de pedidos protegidos por RBAC")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Listar todos los pedidos")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<java.util.List<OrderResponse>> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @Operation(summary = "Listar pedidos de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @GetMapping({"/customer/{customerId}", "/{customerId}"})
    public ResponseEntity<java.util.List<OrderResponse>> getOrdersByCustomerId(@PathVariable @Positive(message = "El id del cliente debe ser mayor que cero") Integer customerId) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId));
    }

    @Operation(summary = "Obtener el detalle de un pedido")
    @PreAuthorize("hasRole('ADMIN') or @resourceAuthorizationService.canAccessOrder(#orderId)")
    @GetMapping("/detail/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable @Positive(message = "El id del pedido debe ser mayor que cero") Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @Operation(summary = "Crear un pedido manualmente desde administracion")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @Operation(summary = "Actualizar un pedido desde administracion")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponse> updateOrder(@PathVariable @Positive(message = "El id del pedido debe ser mayor que cero") Integer orderId,
                                                     @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, request));
    }

    @Operation(summary = "Eliminar un pedido")
    @PreAuthorize("hasRole('ADMIN') or @resourceAuthorizationService.canAccessOrder(#orderId)")
    @DeleteMapping("/{orderId}")
    public ResponseEntity<?> deleteOrder(@PathVariable @Positive(message = "El id del pedido debe ser mayor que cero") Integer orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }
}
