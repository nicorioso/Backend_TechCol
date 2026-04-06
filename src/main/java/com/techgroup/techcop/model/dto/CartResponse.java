package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.Carts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartResponse {

    private Integer cartId;
    private Integer customerId;
    private BigDecimal cartPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CartItemResponse> items;

    public static CartResponse fromEntity(Carts cart, List<CartItemResponse> items) {
        CartResponse response = new CartResponse();
        response.setCartId(cart.getCart_id());
        response.setCustomerId(cart.getCustomer() != null ? cart.getCustomer().getCustomerId() : null);
        response.setCartPrice(cart.getCart_price());
        response.setCreatedAt(cart.getCreate_at());
        response.setUpdatedAt(cart.getUpdatedAt());
        response.setItems(items);
        return response;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public BigDecimal getCartPrice() {
        return cartPrice;
    }

    public void setCartPrice(BigDecimal cartPrice) {
        this.cartPrice = cartPrice;
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

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }
}
