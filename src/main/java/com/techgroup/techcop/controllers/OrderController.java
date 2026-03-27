package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.service.order.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<?> getOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @GetMapping({"/customer/{id}", "/{id}"})
    public ResponseEntity<?> getOrdersByCustomerId(@PathVariable int id) {
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(id));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteOrder(@PathVariable int id){
        try {
            orderService.deleteOrder(id);
            return ResponseEntity.noContent().build();
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}
