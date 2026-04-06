package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.Orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Integer orderId;
    private BigDecimal orderPrice;
    private CustomerResponse customer;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String paypalOrderId;
    private String status;
    private List<OrderItemResponse> orderDetails;

    public static OrderResponse fromEntity(Orders order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setOrderPrice(order.getOrderPrice());
        response.setCustomer(order.getCustomer() != null ? CustomerResponse.fromEntity(order.getCustomer()) : null);
        response.setOrderDate(order.getOrderDate());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setPaypalOrderId(order.getPaypalOrderId());
        response.setStatus(order.getStatus());
        response.setOrderDetails(
                order.getOrderDetails() == null
                        ? List.of()
                        : order.getOrderDetails().stream()
                        .map(OrderItemResponse::fromEntity)
                        .toList()
        );
        return response;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public CustomerResponse getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerResponse customer) {
        this.customer = customer;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPaypalOrderId() {
        return paypalOrderId;
    }

    public void setPaypalOrderId(String paypalOrderId) {
        this.paypalOrderId = paypalOrderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItemResponse> getOrderDetails() {
        return orderDetails;
    }

    public void setOrderDetails(List<OrderItemResponse> orderDetails) {
        this.orderDetails = orderDetails;
    }
}
