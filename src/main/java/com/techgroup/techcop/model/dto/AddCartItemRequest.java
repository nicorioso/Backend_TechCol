package com.techgroup.techcop.model.dto;

public class AddCartItemRequest {

    private Integer productId;
    private Integer quantity;

    public AddCartItemRequest() {}

    public AddCartItemRequest(Integer quantity, Integer productId) {
        this.quantity = quantity;
        this.productId = productId;
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

}