package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.Products;

import java.util.List;
import java.util.Optional;

public interface ProductServiceTest {

    public List<Products> getProducts();
    public Optional<Products> getProduct(int id);
    public Products addProduct(Products products);
    public Products updateProduct(int id, Products products);
    public void deleteProduct(int id);
    public Products patchProduct(int id, Products products);

}


