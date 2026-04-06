package com.techgroup.techcop.service.product.impl;


import com.techgroup.techcop.exception.ProductInUseException;
import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.dto.ProductResponse;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.CartDetailsRepository;
import com.techgroup.techcop.repository.OrderDetailsRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductsRepository productsRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductImageStorageService productImageStorageService;

    public ProductServiceImpl(
            ProductsRepository productsRepository,
            CartDetailsRepository cartDetailsRepository,
            OrderDetailsRepository orderDetailsRepository,
            ProductImageStorageService productImageStorageService) {
        this.productsRepository = productsRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productImageStorageService = productImageStorageService;
    }

    @Override
    public List<ProductResponse> getProducts() {
        return productsRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .toList();
    }

    @Override
    public Optional<ProductResponse> getProduct(int id) {
        return productsRepository.findById(id)
                .map(ProductResponse::fromEntity);
    }

    @Override
    public ProductResponse addProduct(ProductRequest request, MultipartFile image) {

        Products product = new Products();
        applyProductRequest(product, request, image);
        return ProductResponse.fromEntity(productsRepository.save(product));
    }


    @Override
    public ProductResponse updateProduct(int id, ProductRequest request, MultipartFile image) {

        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el producto"));

        applyProductRequest(product, request, image);
        return ProductResponse.fromEntity(productsRepository.save(product));
    }


    @Override
    @Transactional
    public void deleteProduct(int id) {
        Products products = productsRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existe el producto con el id: " + id
                ));

        if (cartDetailsRepository.existsByProductId(id)) {
            throw new ProductInUseException("No se puede eliminar el producto porque existe en carritos activos.");
        }

        if (orderDetailsRepository.existsByProductId(id)) {
            throw new ProductInUseException("No se puede eliminar el producto porque tiene pedidos asociados.");
        }

        productsRepository.delete(products);
    }

    private void applyProductRequest(Products product,
                                     ProductRequest request,
                                     MultipartFile image) {
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        String storedImage = productImageStorageService.store(image);
        if (storedImage != null) {
            product.setImageUrl(storedImage);
        }
    }

}
