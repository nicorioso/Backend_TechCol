package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.dto.AddCartItemRequest;
import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.CartDetailsRepository;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.cart.CartService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

        Products product = productsRepository.findById(cartItem.getProduct_id())
                .orElseThrow(() ->
                        new RuntimeException("No existe el producto con id " + cartItem.getProduct_id())
                );

        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(i -> i.getProduct_id().equals(cartItem.getProduct_id()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            CartItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());

        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct_id(product.getProduct_id());
            newItem.setQuantity(cartItem.getQuantity());
            newItem.setUnit_price(product.getPrice());
            newItem.setCart(cart);

            cart.getItems().add(newItem);
        }

        priceService.recalculateTotal(cart);

        return cartsRepository.save(cart);
    }

    @Override
    public Carts syncCartItems(List<CartItem> items, Integer customerId) {

        Carts cart = validationService.validateCustomerCart(customerId);
        List<CartItem> nextItems = new ArrayList<>();

        for (CartItem incomingItem : items == null ? List.<CartItem>of() : items) {
            if (incomingItem == null || incomingItem.getProduct_id() == null) {
                continue;
            }

            int quantity = incomingItem.getQuantity() == null ? 0 : incomingItem.getQuantity();
            if (quantity <= 0) {
                continue;
            }

            Products product = productsRepository.findById(incomingItem.getProduct_id())
                    .orElseThrow(() ->
                            new RuntimeException("No existe el producto con id " + incomingItem.getProduct_id())
                    );

            CartItem newItem = new CartItem();
            newItem.setProduct_id(product.getProduct_id());
            newItem.setQuantity(quantity);
            newItem.setUnit_price(product.getPrice());
            newItem.setCart(cart);
            nextItems.add(newItem);
        }

        cart.getItems().clear();
        cart.getItems().addAll(nextItems);

        priceService.recalculateTotal(cart);

        return cartsRepository.save(cart);
    }


    @Override
    public void deleteCartItem(Integer cartItemId, Integer customerId) {

        Carts cart = validationService.validateCustomerCart(customerId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCart_item_id().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Item no pertenece al carrito"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cart.getItems().remove(item);
        }

        priceService.recalculateTotal(cart);

        cartsRepository.save(cart);
    }

}
