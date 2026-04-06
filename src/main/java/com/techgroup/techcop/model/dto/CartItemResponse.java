package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Products;

import java.time.LocalDateTime;

public class CartItemResponse {

    private Integer cartItemId;
    private Integer productId;
    private Integer quantity;
    private Double unitPrice;
    private String productName;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartItemResponse fromEntity(CartItem item, Products product) {
        CartItemResponse response = new CartItemResponse();
        response.setCartItemId(item.getCart_item_id());
        response.setProductId(item.getProduct_id());
        response.setQuantity(item.getQuantity());
        response.setUnitPrice(item.getUnit_price());
        response.setProductName(product != null ? product.getProductName() : null);
        response.setImageUrl(product != null ? product.getImageUrl() : null);
        response.setCreatedAt(item.getCreatedAt());
        response.setUpdatedAt(item.getUpdatedAt());
        return response;
    }

    public Integer getCartItemId() {
        return cartItemId;
    }

    public void setCartItemId(Integer cartItemId) {
        this.cartItemId = cartItemId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
}
