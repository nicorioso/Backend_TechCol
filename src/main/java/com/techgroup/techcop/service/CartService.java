package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.Carts;
import com.techgroup.techcop.domain.CartItem;

import java.util.List;
import java.util.Optional;

public interface CartService {

    public List<CartItem> getCartItems();
    public Carts postCartItem(Carts id);
    //public Carts putCartItem(Carts cartItem);
    public void deleteCartItem(Integer id);

}