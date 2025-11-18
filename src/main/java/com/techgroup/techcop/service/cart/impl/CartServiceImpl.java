package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.CartDetailsRepository;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.cart.CartService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartsRepository cartsRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final ProductsRepository productsRepository;
    private final CartValidationService validationService;
    private final CartPriceService priceService;

    public CartServiceImpl(
            CartsRepository cartsRepository,
            CartDetailsRepository cartDetailsRepository,
            ProductsRepository productsRepository,
            CartValidationService validationService,
            CartPriceService priceService) {
        this.cartsRepository = cartsRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.productsRepository = productsRepository;
        this.validationService = validationService;
        this.priceService = priceService;
    }

    @Override
    public List<CartItem> getCartItems(Integer customerId) {
        Carts cart = validationService.validateCustomerCart(customerId);
        return cart.getItems();
    }

    @Override
    public Carts postCartItem(CartItem cartItem, Integer customerId) {
        Carts cart = validationService.validateCustomerCart(customerId);

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProduct_id().equals(cartItem.getProduct_id()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
        } else {
            cartItem.setCart(cart);
            cart.getItems().add(cartItem);
        }

        priceService.recalculateTotal(cart);
        return cartsRepository.save(cart);
    }

    @Override
    public void deleteCartItem(Integer cartItemId, Integer customerId) {
        Carts cart = validationService.validateCustomerCart(customerId);
        CartItem item = cartDetailsRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
            cartDetailsRepository.save(item);
        } else {
            cartDetailsRepository.delete(item);
        }

        priceService.recalculateTotal(cart);
        cartsRepository.save(cart);
    }
}