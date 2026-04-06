package com.techgroup.techcop.controllers;

import com.techgroup.techcop.exception.GlobalExceptionHandler;
import com.techgroup.techcop.model.dto.OrderResponse;
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
import com.techgroup.techcop.service.order.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        RateLimitingFilter.class,
        JwtAuthEntryPoint.class,
        TestCorsConfig.class
})
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void shouldReturnOrdersByCustomer() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setOrderId(25);
        order.setStatus("PAID");

        when(orderService.getOrdersByCustomerId(9)).thenReturn(List.of(order));
        mockCustomerJwt("user-token", 9, "user@test.com");

        mockMvc.perform(
                        get("/order/9")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(25))
                .andExpect(jsonPath("$[0].status").value("PAID"));
    }

    @Test
    void shouldReturnAllOrdersForAdmin() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setOrderId(11);
        order.setStatus("DELIVERED");

        when(orderService.getAllOrders()).thenReturn(List.of(order));
        mockAdminJwt("admin-token", 1, "admin@test.com");

        mockMvc.perform(
                        get("/order")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderId").value(11))
                .andExpect(jsonPath("$[0].status").value("DELIVERED"));
    }

    @Test
    void shouldRejectCustomerRoleForAdminOrderListing() throws Exception {
        mockCustomerJwt("user-token", 9, "user@test.com");

        mockMvc.perform(
                        get("/order")
                                .header("Authorization", "Bearer user-token")
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(orderService);
    }

    @Test
    void shouldCreateOrderForAdmin() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setOrderId(41);
        order.setStatus("PAID");

        when(orderService.createOrder(any())).thenReturn(order);
        mockAdminJwt("admin-token", 1, "admin@test.com");

        mockMvc.perform(
                        post("/order")
                                .header("Authorization", "Bearer admin-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "orderPrice": 250000,
                                          "customerId": 9,
                                          "orderDate": "2026-04-05T16:30:00",
                                          "paypalOrderId": "PAY-123",
                                          "status": "PAID"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(41))
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void shouldUpdateOrderForAdmin() throws Exception {
        OrderResponse order = new OrderResponse();
        order.setOrderId(41);
        order.setStatus("DELIVERED");

        when(orderService.updateOrder(any(), any())).thenReturn(order);
        mockAdminJwt("admin-token", 1, "admin@test.com");

        mockMvc.perform(
                        put("/order/41")
                                .header("Authorization", "Bearer admin-token")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "orderPrice": 250000,
                                          "customerId": 9,
                                          "orderDate": "2026-04-05T16:30:00",
                                          "paypalOrderId": "PAY-123",
                                          "status": "DELIVERED"
                                        }
                                        """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(41))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void shouldDeleteOrderForAdmin() throws Exception {
        mockAdminJwt("admin-token", 1, "admin@test.com");

        mockMvc.perform(
                        delete("/order/41")
                                .header("Authorization", "Bearer admin-token")
                )
                .andExpect(status().isNoContent());

        verify(orderService).deleteOrder(41);
    }

    private void mockCustomerJwt(String token, Integer id, String email) {
        CustomUserDetails customerUser = buildUserDetails(id, email, "ROLE_CLIENTE");
        when(jwtService.extractUsername(token)).thenReturn(email);
        when(customUserDetailsService.loadUserByUsername(email)).thenReturn(customerUser);
        when(jwtService.isTokenValid(token, customerUser)).thenReturn(true);
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
