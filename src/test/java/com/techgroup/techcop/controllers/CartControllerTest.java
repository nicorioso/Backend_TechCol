package com.techgroup.techcop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.model.dto.AddCartItemRequest;
import com.techgroup.techcop.model.dto.CartItemResponse;
import com.techgroup.techcop.model.dto.CartSummaryResponse;
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
import com.techgroup.techcop.service.cart.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CartController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnAllCartsForAdmin() throws Exception {
        CartSummaryResponse cart = new CartSummaryResponse();
        cart.setCartId(7);
        cart.setCustomerId(9);
        cart.setCustomerName("Nicolas");
        cart.setCustomerLastName("Rios");
        cart.setCustomerEmail("nico@test.com");
        cart.setItemCount(3);

        when(cartService.getAllCarts()).thenReturn(List.of(cart));
        mockCustomerJwt("admin-token", 1, "admin@test.com", "ROLE_ADMIN");

        mockMvc.perform(
                        get("/cart")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartId").value(7))
                .andExpect(jsonPath("$[0].customerName").value("Nicolas"))
                .andExpect(jsonPath("$[0].itemCount").value(3));
    }

    @Test
    void shouldForbidCartListForRegularUser() throws Exception {
        mockCustomerJwt("user-token", 9, "user@test.com", "ROLE_CLIENTE");

        mockMvc.perform(
                        get("/cart")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(cartService);
    }

    @Test
    void shouldReturnCartItems() throws Exception {
        CartItemResponse item = new CartItemResponse();
        item.setCartItemId(10);
        item.setProductId(3);
        item.setQuantity(2);
        item.setUnitPrice(150000.0);

        when(cartService.getCartItems(9)).thenReturn(List.of(item));
        mockCustomerJwt("user-token", 9, "user@test.com", "ROLE_CLIENTE");

        mockMvc.perform(
                        get("/cart/9")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cartItemId").value(10))
                .andExpect(jsonPath("$[0].quantity").value(2));
    }

    @Test
    void shouldRejectInvalidCartItemPayload() throws Exception {
        AddCartItemRequest request = new AddCartItemRequest();
        request.setProductId(1);
        request.setQuantity(0);
        mockCustomerJwt("user-token", 9, "user@test.com", "ROLE_CLIENTE");

        mockMvc.perform(post("/cart/9")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error de validacion"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("quantity")));
    }

    @Test
    void shouldDeleteCartItem() throws Exception {
        doNothing().when(cartService).deleteCartItem(15, 9);
        mockCustomerJwt("user-token", 9, "user@test.com", "ROLE_CLIENTE");

        mockMvc.perform(
                        delete("/cart/15/9")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isOk());

        verify(cartService).deleteCartItem(15, 9);
    }

    private void mockCustomerJwt(String token, Integer id, String email, String roleName) {
        CustomUserDetails user = buildUserDetails(id, email, roleName);
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(user);
        when(jwtService.isTokenValid(token, user)).thenReturn(true);
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
