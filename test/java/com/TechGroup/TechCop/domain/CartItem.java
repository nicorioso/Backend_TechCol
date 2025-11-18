package com.techgroup.techcop.model;

public class CartItem {
    private Integer cartsId;
    private Double total;
    private Integer amount;
    private Products products;
    private Customer customer;

    public CartItem(Integer cartsId, Double total, Integer amount, Products products, Customer customer) {
        this.cartsId = cartsId;
        this.total = total;
        this.amount = amount;
        this.products = products;
        this.customer = customer;
    }

    public Integer getCartsId() {
        return cartsId;
    }

    public void setCartsId(Integer cartsId) {
        this.cartsId = cartsId;
    }

    public Double getTotal() {
        return products.getPrice() * amount;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Products getProducts() {
        return products;
    }

    public void setProducts(Products products) {
        this.products = products;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) { this.customer = customer; }
}