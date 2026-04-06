package com.techgroup.techcop.model.dto;

import com.techgroup.techcop.model.entity.CartItem;
import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class CartSummaryResponse {

    private Integer cartId;
    private Integer customerId;
    private String customerName;
    private String customerLastName;
    private String customerEmail;
    private BigDecimal cartPrice;
    private Integer itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CartSummaryResponse fromEntity(Carts cart) {
        CartSummaryResponse response = new CartSummaryResponse();
        Customer customer = cart.getCustomer();
        List<CartItem> items = cart.getItems() == null ? List.of() : cart.getItems();

        response.setCartId(cart.getCart_id());
        response.setCustomerId(customer != null ? customer.getCustomerId() : null);
        response.setCustomerName(customer != null ? customer.getCustomerName() : null);
        response.setCustomerLastName(customer != null ? customer.getCustomerLastName() : null);
        response.setCustomerEmail(customer != null ? customer.getCustomerEmail() : null);
        response.setCartPrice(cart.getCart_price());
        response.setItemCount(items.stream()
                .map(CartItem::getQuantity)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum());
        response.setCreatedAt(cart.getCreate_at());
        response.setUpdatedAt(cart.getUpdatedAt());
        return response;
    }

    public Integer getCartId() {
        return cartId;
    }

    public void setCartId(Integer cartId) {
        this.cartId = cartId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public BigDecimal getCartPrice() {
        return cartPrice;
    }

    public void setCartPrice(BigDecimal cartPrice) {
        this.cartPrice = cartPrice;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
