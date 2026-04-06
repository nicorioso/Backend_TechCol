package com.techgroup.techcop.service.product.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ProductImageStorageService {

    private final Path uploadPath;

    public ProductImageStorageService(@Value("${file.upload-dir}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir);
    }

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            return null;
        }

        try {
            Files.createDirectories(uploadPath);

            String originalFileName = image.getOriginalFilename() == null
                    ? "product-image"
                    : Paths.get(image.getOriginalFilename()).getFileName().toString();

            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path filePath = uploadPath.resolve(storedFileName);

            Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return storedFileName;
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error guardando la imagen",
                    ex
            );
        }
    }
}
