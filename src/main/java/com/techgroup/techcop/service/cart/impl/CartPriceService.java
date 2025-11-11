package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
public class CartPriceService {

    public void recalculateTotal(Carts cart) {
        double total = cart.getItems()
                .stream()
                .mapToDouble(i -> i.getUnit_price() * i.getQuantity())
                .sum();
        cart.setCart_price(total);
    }
}