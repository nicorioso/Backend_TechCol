package com.techgroup.techcop.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "Products")
public class Products {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer product_id;

    @Column(name = "productName")
    @JsonProperty("productName")
    private String productName;

    @Column(name = "productDescription")
    private String description;

    @Column(name = "productPrice")
    private Double price;

    @Column(name = "productStock")
    private Integer stock;

    public Products() {

    }

    public Products(Integer product_id, String productName, String description, Double price, Integer stock) {
        this.product_id = product_id;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    public Integer getId() {
        return product_id;
    }

    public void setId(Integer product_id) {
        this.product_id = product_id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer Stock) {
        this.stock = Stock;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

}