package com.techgroup.techcop.service.cart;

import com.techgroup.techcop.model.dto.AddCartItemRequest;
import com.techgroup.techcop.model.dto.CartItemResponse;
import com.techgroup.techcop.model.dto.CartResponse;
import com.techgroup.techcop.model.dto.CartSummaryResponse;

import java.util.List;

public interface CartService {

    List<CartSummaryResponse> getAllCarts();

    List<CartItemResponse> getCartItems(Integer customerId);

    CartResponse postCartItem(AddCartItemRequest request, Integer customerId);

    CartResponse syncCartItems(List<AddCartItemRequest> items, Integer customerId);

    void deleteCartItem(Integer cartItemId, Integer customerId);

}
