package com.techgroup.techcop.service.product;

import com.techgroup.techcop.model.dto.ProductRequest;
import com.techgroup.techcop.model.entity.Products;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    public List<Products> getProducts();
    public Optional<Products> getProduct(int id);
    Products addProduct(ProductRequest request, MultipartFile image);
    Products updateProduct(int id, ProductRequest request, MultipartFile image);
    public void deleteProduct(int id);

}


