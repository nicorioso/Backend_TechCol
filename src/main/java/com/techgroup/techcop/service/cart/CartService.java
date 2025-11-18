package com.techgroup.techcop.service.cart;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.CartItem;

import java.util.List;

public interface CartService {

    public List<CartItem> getCartItems(Integer customerId);
    public Carts postCartItem(CartItem id, Integer customerId);
    //public Carts putCartItem(Carts cartItem);
    public void deleteCartItem(Integer CartItemId, Integer customerId);

}