package com.techgroup.techcop.controllers;

import com.techgroup.techcop.domain.Products;
import com.techgroup.techcop.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/Products")
public class ProductController {

    @Autowired
    private ProductService productService;

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

    @PostMapping
    public ResponseEntity<Products> addProduct(@RequestBody Products products) {
        Products newProducts = productService.addProduct(products);
        URI location = URI.create("/Products/" + newProducts.getId());
        System.out.println("JSON recibido: " + products);
        return ResponseEntity.created(location).body(newProducts);
    }

    @PutMapping
    public ResponseEntity<?> updateProduct(@RequestBody Products products) {
        try {
            Products update = productService.updateProduct(products.getId(), products);
            return ResponseEntity.ok(update);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
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

    @PatchMapping
    public ResponseEntity<?> patchProduct(@RequestBody Products products) {
        try {
            Products patch = productService.patchProduct(products.getId(), products);
            return ResponseEntity.ok(patch);
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

}