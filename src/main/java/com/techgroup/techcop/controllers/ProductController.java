package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.dto.ProductResponse;
import com.techgroup.techcop.service.product.ProductService;
import com.techgroup.techcop.util.ValidatedJsonRequestParser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@Validated
@RequestMapping("/products")
@Tag(name = "Products", description = "Consulta publica y administracion de productos")
public class ProductController {

    private final ProductService productService;
    private final ValidatedJsonRequestParser validatedJsonRequestParser;

    public ProductController(ProductService productService,
                             ValidatedJsonRequestParser validatedJsonRequestParser) {
        this.productService = productService;
        this.validatedJsonRequestParser = validatedJsonRequestParser;
    }

    @Operation(summary = "Listar productos")
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {
        return ResponseEntity.ok(productService.getProducts());
    }

    @Operation(summary = "Obtener un producto por id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable @Positive(message = "El id del producto debe ser mayor que cero") int id) {
        Optional<ProductResponse> productOpt = productService.getProduct(id);

        return productOpt
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Operation(summary = "Crear un producto")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> addProduct(
            @RequestPart("data") String data,
            @RequestPart(name = "image", required = false) MultipartFile image) {

        ProductRequest request = parseAndValidateProductRequest(data);
        ProductResponse saved = productService.addProduct(request, image);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Actualizar un producto")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable @Positive(message = "El id del producto debe ser mayor que cero") int id,
            @RequestPart("data") String data,
            @RequestPart(name = "image", required = false) MultipartFile image) {

        ProductRequest request = parseAndValidateProductRequest(data);
        ProductResponse updated = productService.updateProduct(id, request, image);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Eliminar un producto")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable @Positive(message = "El id del producto debe ser mayor que cero") int id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    private ProductRequest parseAndValidateProductRequest(String data) {
        return validatedJsonRequestParser.parse(
                data,
                ProductRequest.class,
                "El cuerpo del producto no contiene un JSON valido"
        );
    }

}
