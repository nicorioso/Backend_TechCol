package com.techgroup.techcop.service.order.impl;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.dto.OrderResponse;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.security.access.ResourceAuthorizationService;
import com.techgroup.techcop.service.audit.AuditLogService;
import com.techgroup.techcop.service.order.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ResourceAuthorizationService resourceAuthorizationService;
    private final AuditLogService auditLogService;

    public OrderServiceImpl(OrderRepository orderRepository,
                            CustomerRepository customerRepository,
                            ResourceAuthorizationService resourceAuthorizationService,
                            AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.resourceAuthorizationService = resourceAuthorizationService;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByCustomerId(Integer id) {
        resourceAuthorizationService.assertCurrentCustomer(id);
        return orderRepository.findAllByCustomerCustomerId(id).stream()
                .map(OrderResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId) {
        return OrderResponse.fromEntity(getOrderEntity(orderId));
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Orders order = new Orders();
        LocalDateTime now = LocalDateTime.now();

        order.setCustomer(resolveCustomer(request.getCustomerId()));
        order.setOrderPrice(request.getOrderPrice());
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : now);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setPaypalOrderId(normalizeNullableText(request.getPaypalOrderId()));
        order.setStatus(normalizeStatus(request.getStatus()));

        Orders savedOrder = orderRepository.save(order);
        auditLogService.log(
                getCurrentActorEmail(),
                "ORDER_CREATED",
                "ORDER",
                savedOrder.getOrderId() == null ? null : savedOrder.getOrderId().toString(),
                "Pedido creado desde el panel administrativo con estado " + savedOrder.getStatus()
        );
        return OrderResponse.fromEntity(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponse updateOrder(Integer orderId, OrderRequest request) {
        Orders order = getOrderEntity(orderId);

        order.setCustomer(resolveCustomer(request.getCustomerId()));
        order.setOrderPrice(request.getOrderPrice());
        order.setOrderDate(request.getOrderDate() != null ? request.getOrderDate() : order.getOrderDate());
        order.setPaypalOrderId(normalizeNullableText(request.getPaypalOrderId()));
        order.setStatus(normalizeStatus(request.getStatus()));
        order.setUpdatedAt(LocalDateTime.now());

        Orders updatedOrder = orderRepository.save(order);
        auditLogService.log(
                getCurrentActorEmail(),
                "ORDER_UPDATED",
                "ORDER",
                updatedOrder.getOrderId() == null ? null : updatedOrder.getOrderId().toString(),
                "Pedido actualizado desde el panel administrativo con estado " + updatedOrder.getStatus()
        );
        return OrderResponse.fromEntity(updatedOrder);
    }

    @Override
    @Transactional
    public void deleteOrder(Integer idOrder) {
        Orders order = getOrderEntity(idOrder);
        orderRepository.delete(order);
        auditLogService.log(
                getCurrentActorEmail(),
                "ORDER_DELETED",
                "ORDER",
                idOrder == null ? null : idOrder.toString(),
                "Pedido eliminado desde el panel administrativo"
        );
    }

    private Orders getOrderEntity(Integer orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Order not found"));
        resourceAuthorizationService.assertCanAccessOrder(order);
        return order;
    }

    private Customer resolveCustomer(Integer customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
    }

    private String normalizeNullableText(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }

    private String getCurrentActorEmail() {
        return resourceAuthorizationService.getAuthenticatedCustomer().getCustomerEmail();
    }
}
