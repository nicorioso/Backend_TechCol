package com.techgroup.techcop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "cart_details")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cartDetailId")
    private Integer cart_item_id;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "unitprice")
    private Double unit_price;

    @ManyToOne
    @JoinColumn(name = "cartId")
    private Integer cart_id;

    @Column(name = "productId")
    private Integer product_id;

    public CartItem() {}

    public CartItem(Integer cart_item_id, Integer quantity, Double unit_price, Integer cart_id, Integer product_id) {
        this.cart_item_id = cart_item_id;
        this.quantity = quantity;
        this.unit_price = unit_price;
        this.cart_id = cart_id;
        this.product_id = product_id;
    }

    public Integer getCart_item_id() {
        return cart_item_id;
    }

    public void setCart_item_id(Integer cart_item_id) {
        this.cart_item_id = cart_item_id;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getUnit_price() {
        return unit_price;
    }

    public void setUnit_price(Double unit_price) {
        this.unit_price = unit_price;
    }

    public Integer getCart_id() {
        return cart_id;
    }

    public void setCart_id(Integer cart_id) {
        this.cart_id = cart_id;
    }

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }
}