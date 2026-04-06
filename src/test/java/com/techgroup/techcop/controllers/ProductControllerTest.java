package com.techgroup.techcop.controllers;

import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.model.dto.ProductResponse;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.security.config.SecurityConfig;
import com.techgroup.techcop.security.jwt.JwtAuthEntryPoint;
import com.techgroup.techcop.security.jwt.JwtAuthenticationFilter;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.model.CustomUserDetails;
import com.techgroup.techcop.security.model.CustomUserDetailsService;
import com.techgroup.techcop.security.ratelimit.RateLimitingFilter;
import com.techgroup.techcop.support.TestCorsConfig;
import com.techgroup.techcop.service.product.ProductService;
import com.techgroup.techcop.util.ValidatedJsonRequestParser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        ValidatedJsonRequestParser.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnAllProducts() throws Exception {
        ProductResponse product = new ProductResponse();
        product.setId(1);
        product.setProductId(1);
        product.setProductName("Laptop gamer");
        product.setDescription("RTX 4070");
        product.setPrice(7800.0);
        product.setStock(4);

        when(productService.getProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].description").value("RTX 4070"));
    }

    @Test
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(productService.getProduct(99)).thenReturn(Optional.empty());

        mockMvc.perform(get("/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Product not found"));
    }

    @Test
    void shouldRejectInvalidMultipartPayload() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                """
                {
                  "productName": "",
                  "description": "Mouse gamer",
                  "price": 0,
                  "stock": -1
                }
                """.getBytes(StandardCharsets.UTF_8)
        );

        mockAdminJwt("admin-token", 1, "admin@test.com");

        mockMvc.perform(
                        multipart("/products")
                                .file(data)
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error de validacion"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("productName")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("price")))
                .andExpect(jsonPath("$.errors[*].field", hasItem("stock")));

        verifyNoInteractions(productService);
    }

    private void mockAdminJwt(String token, Integer id, String email) {
        CustomUserDetails adminUser = buildUserDetails(id, email, "ROLE_ADMIN");
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(adminUser);
        when(jwtService.isTokenValid(token, adminUser)).thenReturn(true);
    }

    private CustomUserDetails buildUserDetails(Integer id, String email, String roleName) {
        Customer customer = new Customer();
        customer.setCustomerId(id);
        customer.setCustomerEmail(email);
        customer.setCustomerPassword("$2a$10$abcdefghijklmnopqrstuv");
        customer.setRole(new Role(1, roleName));
        return new CustomUserDetails(customer);
    }
}
