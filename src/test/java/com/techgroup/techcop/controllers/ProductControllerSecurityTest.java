package com.techgroup.techcop.controllers;

import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.model.dto.ProductRequest;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class ProductControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @MockBean
    private ValidatedJsonRequestParser validatedJsonRequestParser;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldAllowPublicProductListingWithoutJwt() throws Exception {
        when(productService.getProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAdminAccessWithValidJwt() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"productName\":\"Mouse gamer\"}".getBytes(StandardCharsets.UTF_8)
        );

        ProductRequest productRequest = new ProductRequest("Mouse gamer", "Sensor optico", 199000.0, 8);
        ProductResponse productResponse = new ProductResponse();
        productResponse.setProductId(8);
        productResponse.setProductName("Mouse gamer");

        CustomUserDetails adminUser = buildUserDetails(1, "admin@test.com", "ROLE_ADMIN");

        when(jwtService.extractUsername("admin-token")).thenReturn("admin@test.com");
        when(customUserDetailsService.loadUserByUsername("admin@test.com")).thenReturn(adminUser);
        when(jwtService.isTokenValid("admin-token", adminUser)).thenReturn(true);
        when(validatedJsonRequestParser.parse(anyString(), eq(ProductRequest.class), anyString())).thenReturn(productRequest);
        when(productService.addProduct(eq(productRequest), any())).thenReturn(productResponse);

        mockMvc.perform(
                        multipart("/products")
                                .file(data)
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(8))
                .andExpect(jsonPath("$.productName").value("Mouse gamer"));
    }

    @Test
    void shouldRejectCustomerRoleForProtectedProductCreation() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"productName\":\"Mouse gamer\"}".getBytes(StandardCharsets.UTF_8)
        );

        CustomUserDetails customerUser = buildUserDetails(2, "user@test.com", "ROLE_CLIENTE");

        when(jwtService.extractUsername("client-token")).thenReturn("user@test.com");
        when(customUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(customerUser);
        when(jwtService.isTokenValid("client-token", customerUser)).thenReturn(true);

        mockMvc.perform(
                        multipart("/products")
                                .file(data)
                                .header("Authorization", "Bearer client-token")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(validatedJsonRequestParser, productService);
    }

    @Test
    void shouldRequireAuthenticationForProtectedProductCreation() throws Exception {
        MockMultipartFile data = new MockMultipartFile(
                "data",
                "data.json",
                MediaType.APPLICATION_JSON_VALUE,
                "{\"productName\":\"Mouse gamer\"}".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/products").file(data))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(validatedJsonRequestParser, productService);
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
