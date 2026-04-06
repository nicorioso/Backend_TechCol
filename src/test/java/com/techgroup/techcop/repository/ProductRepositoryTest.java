package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Products;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class ProductRepositoryTest {

    @Autowired
    private ProductsRepository productsRepository;

    @Test
    void shouldSaveProduct() {
        Products product = new Products();
        product.setProductName("Teclado mecanico");
        product.setDescription("Switch red");
        product.setPrice(320000.0);
        product.setStock(9);

        Products saved = productsRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getProductName()).isEqualTo("Teclado mecanico");
    }

    @Test
    void shouldFindPersistedProductById() {
        Products product = new Products();
        product.setProductName("Mouse");
        product.setDescription("Sensor optico");
        product.setPrice(120000.0);
        product.setStock(15);

        Products saved = productsRepository.save(product);

        assertThat(productsRepository.findById(saved.getId()))
                .isPresent()
                .get()
                .extracting(Products::getDescription)
                .isEqualTo("Sensor optico");
    }
}
