package com.techgroup.techcop.service;


import com.techgroup.techcop.domain.Products;
import com.techgroup.techcop.repository.ProductsDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImplTest implements ProductServiceTest {

    @Autowired
    private ProductsDBA productsDBA;

    @Override
    public List<Products> getProducts() {
        return productsDBA.findAll();
    }

    @Override
    public Optional<Products> getProduct(int id) {
        Optional<Products> product = productsDBA.findById(id);
        if (product.isPresent()) {
            return product;
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Products addProduct(Products products) {
        return productsDBA.save(products);
    }

    @Override
    public Products updateProduct(int id, Products productsNew) {
        Products productsExist = productsDBA.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        productsExist.setProductName(productsNew.getProductName());
        productsExist.setDescription(productsNew.getDescription());
        productsExist.setStock(productsNew.getStock());
        productsExist.setPrice(productsNew.getPrice());
        return productsDBA.save(productsExist);
    }

    @Override
    public void deleteProduct(int id) {
        Products products = productsDBA.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        productsDBA.delete(products);
    }

    @Override
    public Products patchProduct(int id, Products products) {
        Products productsExist = productsDBA.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        if (products.getProductName() != null) {
            productsExist.setProductName(products.getProductName());
        }
        if (products.getDescription() != null) {
            productsExist.setDescription(products.getDescription());
        }
        if (products.getStock() != null) {
            productsExist.setStock(products.getStock());
        }
        if (products.getPrice() != null) {
            productsExist.setPrice(products.getPrice());
        }
        return productsDBA.save(productsExist);
    }

}