package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.Carts;
import com.techgroup.techcop.domain.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartService {

    public List<CartItem> getCartItems(Integer customerId);
    public Carts postCartItem(CartItem id, Integer customerId);
    //public Carts putCartItem(Carts cartItem);
    public void deleteCartItem(Integer CartItemId, Integer customerId);

}