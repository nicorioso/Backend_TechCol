package com.techgroup.techcop.service.cart;

import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.CartDetailsRepository;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    public CartsRepository cartsRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CartDetailsRepository cartDetailsRepository;
    @Autowired
    private ProductsRepository productsRepository;

    @Override
    public List<CartItem> getCartItems(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Carts cart = customer.getCart();
        if (cart == null) {
            throw new RuntimeException("El cliente no tiene carrito");
        }
        return cart.getItems();
    }

    @Override
    public Carts postCartItem(CartItem cartItem, Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
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

        return cartsRepository.save(cart);
    }


    @Override
    public void deleteCartItem(Integer CartItemId, Integer customerId) {
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        Carts cart = customer.getCart();

        CartItem Item = cartDetailsRepository.findById(CartItemId).orElseThrow(() -> new RuntimeException("CartItem no encontrado"));
        Products products = productsRepository.findById(Item.getProduct_id()).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
        if (Item.getQuantity() > 1) {
            Item.setQuantity(Item.getQuantity() - 1);
            Item.setUnit_price(products.getPrice() * Item.getQuantity());
            cartDetailsRepository.save(Item);
        }else {
            cartDetailsRepository.delete(Item);
        }

        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getUnit_price() * i.getQuantity())
                .sum();
        cart.setCart_price(total);

        cartsRepository.save(cart);

    }
}