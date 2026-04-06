package com.techgroup.techcop.service.product;

import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.dto.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<ProductResponse> getProducts();

    Optional<ProductResponse> getProduct(int id);

    ProductResponse addProduct(ProductRequest request, MultipartFile image);

    ProductResponse updateProduct(int id, ProductRequest request, MultipartFile image);

    void deleteProduct(int id);

}


