package com.techgroup.techcop.service.product.impl;


import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.product.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductsRepository productsRepository;
    @Value("${file.upload-dir}")
    private String uploadDir;

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
    public Products addProduct(ProductRequest request, MultipartFile image) {

        Products product = new Products();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImageUrl(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Error guardando la imagen");
            }
        }

        return productsRepository.save(product);
    }


    @Override
    public Products updateProduct(int id, ProductRequest request, MultipartFile image) {

        Products product = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto"));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();

                Path uploadPath = Paths.get(uploadDir);

                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                Path filePath = uploadPath.resolve(fileName);
                Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                product.setImageUrl(fileName);

            } catch (IOException e) {
                throw new RuntimeException("Error guardando la imagen");
            }
        }

        return productsRepository.save(product);
    }


    @Override
    public void deleteProduct(int id) {
        Products products = productsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        productsRepository.delete(products);
    }

}