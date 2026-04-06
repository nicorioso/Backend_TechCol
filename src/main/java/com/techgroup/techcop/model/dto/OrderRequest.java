package com.techgroup.techcop.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrderRequest {
    @NotNull(message = "El total del pedido es obligatorio")
    @PositiveOrZero(message = "El total del pedido no puede ser negativo")
    private BigDecimal orderPrice;

    @NotNull(message = "El cliente es obligatorio")
    @Positive(message = "El cliente debe ser valido")
    private Integer customerId;

    private LocalDateTime orderDate;

    private String paypalOrderId;

    @NotBlank(message = "El estado del pedido es obligatorio")
    private String status;

    public OrderRequest() {}

    public OrderRequest(BigDecimal orderPrice, Integer customerId, LocalDateTime orderDate, String paypalOrderId, String status) {
        this.orderPrice = orderPrice;
        this.customerId = customerId;
        this.orderDate = orderDate;
        this.paypalOrderId = paypalOrderId;
        this.status = status;
    }

    public BigDecimal getOrderPrice() {
        return orderPrice;
    }

    public void setOrderPrice(BigDecimal orderPrice) {
        this.orderPrice = orderPrice;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
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
}
