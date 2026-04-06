package com.techgroup.techcop.config;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Products;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.password.PasswordHashingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.Optional;

@Configuration
@Profile("dev")
public class SeedDataConfiguration {

    @Bean
    public CommandLineRunner initSeedData(RoleRepository roleRepository,
                                          CustomerRepository customerRepository,
                                          CartsRepository cartsRepository,
                                          ProductsRepository productsRepository,
                                          PasswordHashingService passwordHashingService) {
        return args -> {
            Role adminRole = ensureRole(roleRepository, "ROLE_ADMIN");
            Role customerRole = ensureRole(roleRepository, "ROLE_CLIENTE");

            Customer admin = ensureCustomer(
                    customerRepository,
                    "admin@test.com",
                    "Admin",
                    "TechCol",
                    "+573001110001",
                    "Admin12345!",
                    adminRole,
                    passwordHashingService
            );

            Customer user = ensureCustomer(
                    customerRepository,
                    "user@test.com",
                    "Usuario",
                    "Demo",
                    "+573001110002",
                    "User12345!",
                    customerRole,
                    passwordHashingService
            );

            ensureCart(cartsRepository, admin);
            ensureCart(cartsRepository, user);
            ensureDemoProducts(productsRepository);
        };
    }

    private Role ensureRole(RoleRepository roleRepository, String roleName) {
        return roleRepository.findByRoleName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(null, roleName)));
    }

    private Customer ensureCustomer(CustomerRepository customerRepository,
                                    String email,
                                    String firstName,
                                    String lastName,
                                    String phone,
                                    String rawPassword,
                                    Role role,
                                    PasswordHashingService passwordHashingService) {
        Optional<Customer> existingCustomer = customerRepository.findByCustomerEmail(email);
        if (existingCustomer.isPresent()) {
            Customer customer = existingCustomer.get();
            customer.setRole(role);
            customer.setCustomerName(firstName);
            customer.setCustomerLastName(lastName);
            customer.setCustomerPhoneNumber(phone);
            customer.setCustomerPassword(passwordHashingService.hashNewPassword(rawPassword));
            return customerRepository.save(customer);
        }

        Customer customer = new Customer();
        customer.setCustomerEmail(email);
        customer.setCustomerName(firstName);
        customer.setCustomerLastName(lastName);
        customer.setCustomerPhoneNumber(phone);
        customer.setCustomerPassword(passwordHashingService.hashNewPassword(rawPassword));
        customer.setRole(role);
        return customerRepository.save(customer);
    }

    private void ensureCart(CartsRepository cartsRepository, Customer customer) {
        if (customer == null || customer.getCustomerId() == null) {
            return;
        }

        if (cartsRepository.findByCustomer(customer).isPresent()) {
            return;
        }

        Carts cart = new Carts();
        cart.setCustomer(customer);
        cart.setCart_price(BigDecimal.ZERO);
        cartsRepository.save(cart);
    }

    private void ensureDemoProducts(ProductsRepository productsRepository) {
        if (productsRepository.count() > 0) {
            return;
        }

        productsRepository.save(buildProduct(
                "Procesador Ryzen 7 7800X3D",
                "Procesador gamer de alto rendimiento para equipos de escritorio.",
                1899000.0,
                8
        ));
        productsRepository.save(buildProduct(
                "Tarjeta grafica RTX 4070 Super",
                "GPU para gaming y creacion de contenido con soporte de ray tracing.",
                3299000.0,
                6
        ));
        productsRepository.save(buildProduct(
                "SSD NVMe 1TB PCIe 4.0",
                "Unidad de almacenamiento de alta velocidad para sistema operativo y juegos.",
                389000.0,
                20
        ));
    }

    private Products buildProduct(String name, String description, Double price, Integer stock) {
        Products product = new Products();
        product.setProductName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        return product;
    }
}
