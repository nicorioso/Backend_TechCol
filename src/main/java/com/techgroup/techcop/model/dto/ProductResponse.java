package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.Products;

import java.time.LocalDateTime;

public class ProductResponse {

    private static final String PUBLIC_UPLOAD_PREFIX = "/uploads/products/";

    private Integer id;
    private Integer productId;
    private String productName;
    private String description;
    private Double price;
    private Integer stock;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static ProductResponse fromEntity(Products product) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getProductName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setStock(product.getStock());
        response.setImageUrl(toPublicImageUrl(product.getImageUrl()));
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());
        return response;
    }

    private static String toPublicImageUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return imageUrl;
        }

        if (imageUrl.startsWith("http://") || imageUrl.startsWith("https://") || imageUrl.startsWith(PUBLIC_UPLOAD_PREFIX)) {
            return imageUrl;
        }

        String normalized = imageUrl.startsWith("/") ? imageUrl.substring(1) : imageUrl;
        if (normalized.startsWith("uploads/products/")) {
            return "/" + normalized;
        }

        if (normalized.startsWith("products/")) {
            return "/uploads/" + normalized;
        }

        return PUBLIC_UPLOAD_PREFIX + normalized;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
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
