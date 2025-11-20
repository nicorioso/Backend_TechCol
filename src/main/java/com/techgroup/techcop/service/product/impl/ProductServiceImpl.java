package com.techgroup.techcop.service.product.impl;


import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductsRepository productsRepository;

    public ProductServiceImpl(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @Override
    public List<Products> getProducts() {
        return productsRepository.findAll();
    }

    @Override
    public Optional<Products> getProduct(int id) {
        Optional<Products> product = productsRepository.findById(id);
        if (product.isPresent()) {
            return product;
        }
        else {
            return Optional.empty();
        }
    }

    @Override
    public Products addProduct(Products products) {
        return productsRepository.save(products);
    }

    @Override
    public Products updateProduct(int id, Products productsNew) {
        Products productsExist = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        return productsRepository.save(productsExist);
    }

    @Override
    public void deleteProduct(int id) {
        Products products = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        productsRepository.delete(products);
    }

}