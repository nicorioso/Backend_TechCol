package com.techgroup.techcop.service.payment.impl;

import com.techgroup.techcop.model.entity.*;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.OrderDetailsRepository;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.service.payment.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.techgroup.techcop.repository.CartsRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final CartsRepository cartsRepository;
    private final OrderDetailsRepository orderDetailsRepository;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    public PaymentServiceImpl(CustomerRepository customerRepository,
                              OrderRepository orderRepository,
                              RestTemplate restTemplate,
                              CartsRepository cartsRepository,
                              OrderDetailsRepository orderDetailsRepository) {

        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.cartsRepository = cartsRepository;
        this.orderDetailsRepository = orderDetailsRepository;
    }

    @Override
    public Map<String, String> createPayPalOrder() {

        Customer customer = getAuthenticatedCustomer();
        Carts cart = getCustomerCart(customer);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalDouble = cart.getCart_price();
        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        Map<String, Object> amount = new HashMap<>();
        amount.put("currency_code", "USD");
        amount.put("value", totalDouble.toString());

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amount);

        Map<String, Object> body = new HashMap<>();
        body.put("intent", "CAPTURE");
        body.put("purchase_units", new Object[]{purchaseUnit});

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v2/checkout/orders",
                HttpMethod.POST,
                request,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();

        String orderId = responseBody.get("id").toString();

        // 🔥 Extraer approve URL
        String approveUrl = null;

        var links = (java.util.List<Map<String, Object>>) responseBody.get("links");

        for (Map<String, Object> link : links) {
            if (link.get("rel").equals("approve")) {
                approveUrl = link.get("href").toString();
                break;
            }
        }

        if (approveUrl == null) {
            throw new RuntimeException("No approve link returned by PayPal");
        }

        Map<String, String> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("approveUrl", approveUrl);

        return result;
    }

    @Override
    public void captureOrder(String paypalOrderId) {

        Customer customer = getAuthenticatedCustomer();
        Carts cart = getCustomerCart(customer);

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // 🔥 IMPORTANTE
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>("{}", headers); // 🔥 body vacío JSON

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v2/checkout/orders/" + paypalOrderId + "/capture",
                HttpMethod.POST,
                request,
                Map.class
        );

        String status = response.getBody().get("status").toString();

        if (!status.equals("COMPLETED")) {
            throw new RuntimeException("Payment not completed");
        }

        BigDecimal totalDouble = cart.getCart_price();

        Orders order = new Orders();
        order.setCustomer(customer);
        order.setOrderPrice(totalDouble);
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setPaypalOrderId(paypalOrderId);
        order.setStatus("PAID");

        Orders savedOrder = orderRepository.save(order);

        Products product = new Products();

        cart.getItems().forEach(item -> {

            OrderDetails detail = new OrderDetails();
            detail.setOrder(savedOrder);
            product.setProduct_id(item.getProduct_id());
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(item.getUnit_price()));
            detail.setCreatedAt(LocalDateTime.now());
            detail.setUpdatedAt(LocalDateTime.now());

            orderDetailsRepository.save(detail);

        });

        cart.getItems().clear();
        cart.setCart_price(BigDecimal.valueOf(0.0));
        cartsRepository.save(cart);
    }

    private String getAccessToken() {

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request =
                new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v1/oauth2/token",
                HttpMethod.POST,
                request,
                Map.class
        );

        return response.getBody().get("access_token").toString();
    }

    private Customer getAuthenticatedCustomer() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Carts getCustomerCart(Customer customer) {
        return cartsRepository.findByCustomer(customer)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
    }
}