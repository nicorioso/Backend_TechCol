package com.techgroup.techcop.service.payment.impl;

import com.techgroup.techcop.model.dto.PaymentCallbackResponse;
import com.techgroup.techcop.model.entity.*;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.OrderDetailsRepository;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.repository.ProductsRepository;
import com.techgroup.techcop.service.payment.PaymentService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.techgroup.techcop.repository.CartsRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final CartsRepository cartsRepository;
    private final OrderDetailsRepository orderDetailsRepository;
    private final ProductsRepository productsRepository;

    @Value("${paypal.client-id}")
    private String clientId;

    @Value("${paypal.client-secret}")
    private String clientSecret;

    @Value("${paypal.base-url}")
    private String baseUrl;

    @Value("${paypal.return-url}")
    private String returnUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Value("${paypal.frontend-return-url:}")
    private String frontendReturnUrl;

    @Value("${paypal.frontend-cancel-url:}")
    private String frontendCancelUrl;

    public PaymentServiceImpl(CustomerRepository customerRepository,
                              OrderRepository orderRepository,
                              RestTemplate restTemplate,
                              CartsRepository cartsRepository,
                              OrderDetailsRepository orderDetailsRepository,
                              ProductsRepository productsRepository) {

        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
        this.cartsRepository = cartsRepository;
        this.orderDetailsRepository = orderDetailsRepository;
        this.productsRepository = productsRepository;
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
        if (hasText(returnUrl) && hasText(cancelUrl)) {
            Map<String, Object> experienceContext = new HashMap<>();
            experienceContext.put("return_url", returnUrl);
            experienceContext.put("cancel_url", cancelUrl);
            experienceContext.put("user_action", "PAY_NOW");

            Map<String, Object> paypalPaymentSource = new HashMap<>();
            paypalPaymentSource.put("experience_context", experienceContext);

            Map<String, Object> paymentSource = new HashMap<>();
            paymentSource.put("paypal", paypalPaymentSource);
            body.put("payment_source", paymentSource);
        }

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    resolvePaypalApiBaseUrl() + "/v2/checkout/orders",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("PayPal create order failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("PayPal create order request failed", e);
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("id") == null) {
            throw new RuntimeException("PayPal did not return a valid order");
        }

        String orderId = responseBody.get("id").toString();

        // 🔥 Extraer approve URL
        String approveUrl = null;

        List<String> availableRelations = new ArrayList<>();
        Object linksObject = responseBody.get("links");

        if (linksObject instanceof List<?> rawLinks) {
            for (Object rawLink : rawLinks) {
                if (!(rawLink instanceof Map<?, ?> rawMap)) {
                    continue;
                }

                Object relObject = rawMap.get("rel");
                Object hrefObject = rawMap.get("href");
                String rel = relObject != null ? relObject.toString() : "";

                if (hasText(rel)) {
                    availableRelations.add(rel);
                }

                if (!hasText(rel) || hrefObject == null) {
                    continue;
                }

                if ("approve".equalsIgnoreCase(rel) || "payer-action".equalsIgnoreCase(rel)) {
                    approveUrl = hrefObject.toString();
                    break;
                }
            }
        }

        if (approveUrl == null) {
            throw new RuntimeException("PayPal order created without approval link. Returned rel values: " + availableRelations);
        }

        Map<String, String> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("approveUrl", approveUrl);

        return result;
    }

    @Override
    public void captureOrder(String paypalOrderId) {

        if (orderRepository.findByPaypalOrderId(paypalOrderId).isPresent()) {
            return;
        }

        Customer customer = getAuthenticatedCustomer();
        Carts cart = getCustomerCart(customer);
        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        String accessToken = getAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON); // 🔥 IMPORTANTE
        headers.setBearerAuth(accessToken);

        HttpEntity<String> request = new HttpEntity<>("{}", headers); // 🔥 body vacío JSON

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    resolvePaypalApiBaseUrl() + "/v2/checkout/orders/" + paypalOrderId + "/capture",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
        } catch (HttpStatusCodeException e) {
            throw new RuntimeException("PayPal capture failed: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new RuntimeException("PayPal capture request failed", e);
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("status") == null) {
            throw new RuntimeException("PayPal did not return a valid capture response");
        }

        String status = responseBody.get("status").toString();

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

        List<OrderDetails> details = new ArrayList<>();

        cart.getItems().forEach(item -> {
            Products product = productsRepository.findById(item.getProduct_id())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.getProduct_id()));

            OrderDetails detail = new OrderDetails();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setUnitPrice(BigDecimal.valueOf(item.getUnit_price()));
            detail.setCreatedAt(LocalDateTime.now());
            detail.setUpdatedAt(LocalDateTime.now());
            details.add(detail);
        });

        order.setOrderDetails(details);
        Orders savedOrder = orderRepository.save(order);
        orderDetailsRepository.saveAll(savedOrder.getOrderDetails());

        cart.getItems().clear();
        cart.setCart_price(BigDecimal.ZERO);
        cartsRepository.save(cart);
    }

    @Override
    public PaymentCallbackResponse handlePaypalReturn(String token, String payerId) {
        PaymentCallbackResponse response = new PaymentCallbackResponse();
        response.setStatus("approved");
        response.setPaypalOrderId(token);
        response.setPayerId(payerId);
        response.setMessage("PayPal approval received. Call POST /api/paypal/capture/{token} from the frontend.");

        if (hasText(frontendReturnUrl)) {
            response.setRedirectUrl(
                    UriComponentsBuilder.fromUriString(frontendReturnUrl)
                            .queryParam("token", token)
                            .queryParam("status", "approved")
                            .queryParamIfPresent("payerId", java.util.Optional.ofNullable(payerId))
                            .build(true)
                            .toUriString()
            );
        }

        return response;
    }

    @Override
    public PaymentCallbackResponse handlePaypalCancel(String token) {
        PaymentCallbackResponse response = new PaymentCallbackResponse();
        response.setStatus("cancelled");
        response.setPaypalOrderId(token);
        response.setMessage("The user cancelled the PayPal payment.");

        if (hasText(frontendCancelUrl)) {
            response.setRedirectUrl(
                    UriComponentsBuilder.fromUriString(frontendCancelUrl)
                            .queryParam("status", "cancelled")
                            .queryParamIfPresent("token", java.util.Optional.ofNullable(token))
                            .build(true)
                            .toUriString()
            );
        }

        return response;
    }

    private String getAccessToken() {
        if (!hasText(clientId) || !hasText(clientSecret)) {
            throw new RuntimeException("PayPal credentials are missing. Configure PAYPAL_ID and PAYPAL_SECRET in the backend environment.");
        }

        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder()
                .encodeToString(auth.getBytes(StandardCharsets.UTF_8));

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<String> request =
                new HttpEntity<>("grant_type=client_credentials", headers);

        ResponseEntity<Map> response;
        try {
            response = restTemplate.exchange(
                    resolvePaypalApiBaseUrl() + "/v1/oauth2/token",
                    HttpMethod.POST,
                    request,
                    Map.class
            );
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            String details = hasText(responseBody) ? responseBody : e.getStatusText();
            throw new RuntimeException(
                    "PayPal token request failed (" + e.getStatusCode().value() + "): " + details,
                    e
            );
        } catch (Exception e) {
            throw new RuntimeException("PayPal token request could not reach the API. Verify paypal.base-url and credentials.", e);
        }

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null || responseBody.get("access_token") == null) {
            throw new RuntimeException("PayPal did not return an access token");
        }

        return responseBody.get("access_token").toString();
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

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String resolvePaypalApiBaseUrl() {
        String configuredBaseUrl = hasText(baseUrl) ? baseUrl.trim() : "https://api-m.sandbox.paypal.com";

        if (configuredBaseUrl.contains("sandbox.paypal.com")) {
            return "https://api-m.sandbox.paypal.com";
        }

        if (configuredBaseUrl.contains("paypal.com") && !configuredBaseUrl.contains("api-m.")) {
            return configuredBaseUrl.replace("https://", "https://api-m.");
        }

        return configuredBaseUrl;
    }
}
