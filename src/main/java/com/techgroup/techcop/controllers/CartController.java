package com.techgroup.techcop.controllers;

import com.techgroup.techcop.domain.Carts;
import com.techgroup.techcop.domain.CartItem;
import com.techgroup.techcop.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping("/{customerId}")
    public ResponseEntity<List<CartItem>> getCartService(@PathVariable Integer customerId) {
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }

    @PostMapping("/{customerId}")
    public ResponseEntity<?> postCartService(@RequestBody CartItem cartItem,  @PathVariable Integer customerId) {
        try {
            return ResponseEntity.ok().body(cartService.postCartItem(cartItem, customerId));
        }catch (Exception e) {
            Carts carts = cartItem.getCart();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el producto con el id " + carts.getCart_id());
        }
    }

    @DeleteMapping("/{CartItemId}/{CustomerId}")
    public ResponseEntity<?> deleteCartService(@PathVariable Integer CartItemId, @PathVariable Integer CustomerId) {
        cartService.deleteCartItem(CartItemId, CustomerId);
        return ResponseEntity.ok().build();
    }

}