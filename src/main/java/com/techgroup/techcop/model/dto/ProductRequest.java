package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductRequest {

    @JsonProperty("productName")
    private String productName;

    private String description;
    private Double price;
    private Integer stock;

    public ProductRequest() {
    }

    public ProductRequest(String productName, String description, Double price, Integer stock) {
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
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
}
