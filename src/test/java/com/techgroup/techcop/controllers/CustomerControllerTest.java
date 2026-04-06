package com.techgroup.techcop.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.model.dto.CustomerProfileUpdateRequest;
import com.techgroup.techcop.model.dto.CustomerResponse;
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
import com.techgroup.techcop.service.customer.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnAllCustomers() throws Exception {
        CustomerResponse customer = new CustomerResponse();
        customer.setCustomerId(1);
        customer.setCustomerName("Ana");
        customer.setCustomerLastName("Lopez");
        customer.setCustomerEmail("ana@techcol.com");

        when(customerService.getCustomer()).thenReturn(List.of(customer));
        mockUser("admin-token", 1, "admin@test.com", "ROLE_ADMIN");

        mockMvc.perform(
                        get("/customers")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(1))
                .andExpect(jsonPath("$[0].customerEmail").value("ana@techcol.com"));
    }

    @Test
    void shouldReturnNotFoundWhenCustomerDoesNotExist() throws Exception {
        when(customerService.getCustomerById(44)).thenReturn(Optional.empty());
        mockUser("admin-token", 1, "admin@test.com", "ROLE_ADMIN");

        mockMvc.perform(
                        get("/customers/44")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Customer not found"));
    }

    @Test
    void shouldRejectInvalidCustomerUpdatePayload() throws Exception {
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest();
        request.setCustomerName("Ana");
        request.setCustomerLastName("Lopez");
        request.setCustomerEmail("correo-invalido");
        mockUser("user-token", 5, "ana@techcol.com", "ROLE_CLIENTE");

        mockMvc.perform(put("/customers/5")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Error de validacion"))
                .andExpect(jsonPath("$.errors[*].field", hasItem("customerEmail")));
    }

    @Test
    void shouldUpdateCustomerProfile() throws Exception {
        CustomerProfileUpdateRequest request = new CustomerProfileUpdateRequest();
        request.setCustomerName("Ana");
        request.setCustomerLastName("Lopez");
        request.setCustomerEmail("ana@techcol.com");
        request.setCustomerPhoneNumber("+573001112233");

        CustomerResponse response = new CustomerResponse();
        response.setCustomerId(5);
        response.setCustomerName("Ana");
        response.setCustomerLastName("Lopez");
        response.setCustomerEmail("ana@techcol.com");
        response.setCustomerPhoneNumber("+573001112233");

        when(customerService.updateCustomer(eq(5), any(CustomerProfileUpdateRequest.class))).thenReturn(response);
        mockUser("user-token", 5, "ana@techcol.com", "ROLE_CLIENTE");

        mockMvc.perform(put("/customers/5")
                        .header("Authorization", "Bearer user-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(5))
                .andExpect(jsonPath("$.customerPhoneNumber").value("+573001112233"));
    }

    private void mockUser(String token, Integer id, String email, String roleName) {
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
