package com.techgroup.techcop.service;

import com.techgroup.techcop.model.CartItem;
import com.techgroup.techcop.model.Carts;

import java.util.List;

public interface CartServiceTest {

    public List<CartItem> getCartItems();
    public Carts postCartItem(Carts id);
    //public Carts putCartItem(Carts cartItem);
    public void deleteCartItem(Integer id);

}