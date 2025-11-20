package com.techgroup.techcop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.service.product.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<Products>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProductById(@PathVariable int id) {
        Optional<Products> productOpt = productService.getProduct(id);

        return productOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestPart("data") String dataJson,
            @RequestPart(name = "image", required = false) MultipartFile image) {

        try {
            ObjectMapper mapper = new ObjectMapper();
            ProductRequest data = mapper.readValue(dataJson, ProductRequest.class);

            Products p = new Products();
            p.setProductName(data.getProductName());
            p.setDescription(data.getDescription());
            p.setPrice(data.getPrice());
            p.setStock(data.getStock());

            if (image != null && !image.isEmpty()) {
                p.setImage(image.getBytes());
            }

            Products saved = productService.addProduct(p);
            return ResponseEntity.ok(saved);

        } catch (Exception e) {
            return ResponseEntity.status(400).body("Error procesando JSON: " + e.getMessage());
        }
    }



    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable int id,
            @RequestPart("data") ProductRequest data,
            @RequestPart(name = "image", required = false) MultipartFile image) {

        Optional<Products> opt = productService.getProduct(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Products p = opt.get();

        p.setProductName(data.getProductName());
        p.setDescription(data.getDescription());
        p.setPrice(data.getPrice());
        p.setStock(data.getStock());

        if (image != null && !image.isEmpty()) {
            try {
                p.setImage(image.getBytes());
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Error almacenando imagen");
            }
        }

        return ResponseEntity.ok(productService.addProduct(p));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable int id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

}