package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.CartItem;
import com.techgroup.techcop.domain.Carts;
import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.domain.Products;
import com.techgroup.techcop.repository.CartDetailsDBA;
import com.techgroup.techcop.repository.CartsDBA;
import com.techgroup.techcop.repository.CustomerDBA;
import com.techgroup.techcop.repository.ProductsDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    public CartsDBA cartsDBA;
    @Autowired
    private CustomerDBA customerDBA;
    @Autowired
    private CartDetailsDBA cartDetailsDBA;
    @Autowired
    private ProductsDBA productsDBA;

    @Override
    public List<CartItem> getCartItems(Integer customerId) {
        Customer customer = customerDBA.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Carts cart = customer.getCart();
        if (cart == null) {
            throw new RuntimeException("El cliente no tiene carrito");
        }
        return cart.getItems();
    }

    @Override
    public Carts postCartItem(CartItem cartItem, Integer customerId) {
        Customer customer = customerDBA.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Carts cart = customer.getCart();
        if (cart == null) {
            throw new RuntimeException("El cliente no tiene carrito asignado");
        }

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProduct_id().equals(cartItem.getProduct_id()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
            cart.setCart_price(cart.getCart_price() + (cartItem.getQuantity() * cartItem.getUnit_price()));
        } else {
            CartItem newItem = new CartItem();
            newItem.setQuantity(cartItem.getQuantity());
            newItem.setProduct_id(cartItem.getProduct_id());
            newItem.setUnit_price(cartItem.getUnit_price());
            newItem.setCart(cart);
            cart.setCart_price(cart.getCart_price() + (newItem.getQuantity() * newItem.getUnit_price()));

            cart.getItems().add(newItem);
        }

        return cartsDBA.save(cart);
    }


    @Override
    public void deleteCartItem(Integer CartItemId, Integer customerId) {
        Customer customer = customerDBA.findById(customerId).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Carts cart = customer.getCart();

        CartItem Item = cartDetailsDBA.findById(CartItemId).orElseThrow(() -> new RuntimeException("CartItem no encontrado"));
        Products products = productsDBA.findById(Item.getProduct_id()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (Item.getQuantity() > 1) {
            Item.setQuantity(Item.getQuantity() - 1);
            Item.setUnit_price(products.getPrice() * Item.getQuantity());
            cartDetailsDBA.save(Item);
        }else {
            cartDetailsDBA.delete(Item);
        }

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnit_price() * i.getQuantity())
                .sum();
        cart.setCart_price(total);

        cartsDBA.save(cart);

//        Carts cart = cartsDBA.findById(cartId)
//                .orElseThrow(() -> new RuntimeException("No existe el carrito con id: " + cartId));
//        if (cart.getAmount() > 1) {
//            cart.setAmount(cart.getAmount() - 1);
//            Products products = productsDBA.findById(cart.getProduct_id())
//                    .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + cart.getProduct_id()));
//            cart.setCart_price(cart.getCart_price() - products.getPrice());
//            cartsDBA.save(cart);
//        } else {
//            cartsDBA.deleteById(cartId);
//        }
    }
}