package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Products;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImageUrlMappingTest {

    @Test
    void shouldPrefixStoredProductImageFileNameWithPublicUploadsPath() {
        Products product = new Products();
        product.setId(1);
        product.setImageUrl("example.png");

        ProductResponse response = ProductResponse.fromEntity(product);

        assertEquals("/uploads/products/example.png", response.getImageUrl());
    }

    @Test
    void shouldReuseExistingPublicUploadsPathForCartItemResponse() {
        CartItem item = new CartItem();
        Products product = new Products();
        product.setImageUrl("/uploads/products/example.png");

        CartItemResponse response = CartItemResponse.fromEntity(item, product);

        assertEquals("/uploads/products/example.png", response.getImageUrl());
    }
}
