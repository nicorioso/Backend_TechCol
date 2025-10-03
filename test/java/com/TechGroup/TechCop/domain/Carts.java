package com.techgroup.techcop.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "Carts")
public class Carts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cart_id;
    private Double cart_price;
    private Integer product_id;
    private Integer customer_id;
    private Integer amount;

    public Carts() {}

    public Carts(Integer cart_id, Double cart_price, Integer product_id, Integer customer_id) {
        this.cart_id = cart_id;
        this.cart_price = cart_price;
        this.product_id = product_id;
        this.customer_id = customer_id;
    }

    public Integer getCart_id() {
        return cart_id;
    }

    public void setCart_id(Integer cart_id) {
        this.cart_id = cart_id;
    }

    public Double getCart_price() {
        return cart_price;
    }

    public void setCart_price(Double cart_price) {
        this.cart_price = cart_price;
    }

    public Integer getProduct_id() {
        return product_id;
    }

    public void setProduct_id(Integer product_id) {
        this.product_id = product_id;
    }

    public Integer getCustomer_id() {
        return customer_id;
    }

    public void setCustomer_id(Integer customer_id) {
        this.customer_id = customer_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
    //private Products products;
    //private int cantidad;

    /*public Carts(Products products, int cantidad) {
        this.products = products;
        this.cantidad = cantidad;
    }

    // Getters y Setters
    public Products getProduct() {
        return products;
    }

    public void setProduct(Products products) {
        this.products = products;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return products.getPrice() * cantidad;
    }*/

}