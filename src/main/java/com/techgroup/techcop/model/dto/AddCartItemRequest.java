package com.techgroup.techcop.model.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddCartItemRequest {

    @JsonAlias({"product_id", "productId", "id"})
    @NotNull(message = "El producto es obligatorio")
    @Min(value = 1, message = "El producto debe ser valido")
    private Integer productId;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
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
