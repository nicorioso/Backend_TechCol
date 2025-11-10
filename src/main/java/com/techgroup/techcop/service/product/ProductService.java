package com.techgroup.techcop.service.product;

import com.techgroup.techcop.model.entity.Products;
import java.util.List;
import java.util.Optional;

public interface ProductService {

    public List<Products> getProducts();
    public Optional<Products> getProduct(int id);
    public Products addProduct(Products products);
    public Products updateProduct(int id, Products products);
    public void deleteProduct(int id);
    public Products patchProduct(int id, Products products);

}


