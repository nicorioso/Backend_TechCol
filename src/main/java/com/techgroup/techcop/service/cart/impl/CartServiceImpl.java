package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.dto.AddCartItemRequest;
import com.techgroup.techcop.model.dto.CartItemResponse;
import com.techgroup.techcop.model.dto.CartResponse;
import com.techgroup.techcop.model.dto.CartSummaryResponse;
import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.CartDetailsRepository;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.cart.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@Transactional
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
    @Transactional(readOnly = true)
    public List<CartSummaryResponse> getAllCarts() {
        return cartsRepository.findAll().stream()
                .map(CartSummaryResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CartItemResponse> getCartItems(Integer customerId) {
        Carts cart = validationService.validateCustomerCart(customerId);
        return toCartItemResponses(cart.getItems());
    }

    @Override
    public CartResponse postCartItem(AddCartItemRequest request, Integer customerId) {

        Carts cart = validationService.validateCustomerCart(customerId);
        CartItem cartItem = toCartItem(request);

        Products product = productsRepository.findById(cartItem.getProduct_id())
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND,
                        "No existe el producto con id " + cartItem.getProduct_id()
                ));

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

        return toCartResponse(cartsRepository.save(cart));
    }

    @Override
    public CartResponse syncCartItems(List<AddCartItemRequest> items, Integer customerId) {

        Carts cart = validationService.validateCustomerCart(customerId);
        List<CartItem> nextItems = new ArrayList<>();

        for (AddCartItemRequest request : items == null ? List.<AddCartItemRequest>of() : items) {
            if (request == null || request.getProductId() == null) {
                continue;
            }

            int quantity = request.getQuantity() == null ? 0 : request.getQuantity();
            if (quantity <= 0) {
                continue;
            }

            Products product = productsRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            NOT_FOUND,
                            "No existe el producto con id " + request.getProductId()
                    ));

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

        return toCartResponse(cartsRepository.save(cart));
    }


    @Override
    public void deleteCartItem(Integer cartItemId, Integer customerId) {

        Carts cart = validationService.validateCustomerCart(customerId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getCart_item_id().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Item no pertenece al carrito"));

        if (item.getQuantity() > 1) {
            item.setQuantity(item.getQuantity() - 1);
        } else {
            cart.getItems().remove(item);
        }

        priceService.recalculateTotal(cart);

        cartsRepository.save(cart);
    }

    private CartItem toCartItem(AddCartItemRequest request) {
        CartItem cartItem = new CartItem();
        cartItem.setProduct_id(request.getProductId());
        cartItem.setQuantity(request.getQuantity());
        return cartItem;
    }

    private CartResponse toCartResponse(Carts cart) {
        return CartResponse.fromEntity(cart, toCartItemResponses(cart.getItems()));
    }

    private List<CartItemResponse> toCartItemResponses(List<CartItem> items) {
        List<CartItem> safeItems = items == null ? List.of() : items;
        List<Integer> productIds = safeItems.stream()
                .map(CartItem::getProduct_id)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Integer, Products> productsById = productsRepository.findAllById(productIds).stream()
                .collect(Collectors.toMap(Products::getId, Function.identity()));

        return safeItems.stream()
                .map(item -> CartItemResponse.fromEntity(item, productsById.get(item.getProduct_id())))
                .toList();
    }

}
