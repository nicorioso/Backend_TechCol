package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.AddCartItemRequest;
import com.techgroup.techcop.model.dto.CartItemResponse;
import com.techgroup.techcop.model.dto.CartResponse;
import com.techgroup.techcop.model.dto.CartSummaryResponse;
import com.techgroup.techcop.service.cart.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/cart")
@Tag(name = "Cart", description = "Operacion segura del carrito por cliente autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Obtener todos los carritos")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<CartSummaryResponse>> getAllCarts() {
        return ResponseEntity.ok(cartService.getAllCarts());
    }

    @Operation(summary = "Obtener el carrito de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @GetMapping("/{customerId}")
    public ResponseEntity<List<CartItemResponse>> getCartService(@PathVariable @Positive(message = "El id del cliente debe ser mayor que cero") Integer customerId) {
        return ResponseEntity.ok(cartService.getCartItems(customerId));
    }

    @Operation(summary = "Agregar un item al carrito de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @PostMapping("/{customerId}")
    public ResponseEntity<CartResponse> postCartService(@Valid @RequestBody AddCartItemRequest cartItem,
                                                        @PathVariable @Positive(message = "El id del cliente debe ser mayor que cero") Integer customerId) {
        return ResponseEntity.ok(cartService.postCartItem(cartItem, customerId));
    }

    @Operation(summary = "Sincronizar los items del carrito de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @PutMapping("/{customerId}/sync")
    public ResponseEntity<CartResponse> syncCartService(@RequestBody List<@Valid AddCartItemRequest> items,
                                                        @PathVariable @Positive(message = "El id del cliente debe ser mayor que cero") Integer customerId) {
        return ResponseEntity.ok(cartService.syncCartItems(items, customerId));
    }

    @Operation(summary = "Eliminar o decrementar un item del carrito")
    @PreAuthorize("hasRole('ADMIN') or #customerId == authentication.principal.id")
    @DeleteMapping("/{cartItemId}/{customerId}")
    public ResponseEntity<?> deleteCartService(@PathVariable @Positive(message = "El id del item debe ser mayor que cero") Integer cartItemId,
                                               @PathVariable @Positive(message = "El id del cliente debe ser mayor que cero") Integer customerId) {
        cartService.deleteCartItem(cartItemId, customerId);
        return ResponseEntity.ok().build();
    }
}
