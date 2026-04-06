package com.techgroup.techcop.service.order;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    List<OrderResponse> getAllOrders();

    List<OrderResponse> getOrdersByCustomerId(Integer id);

    OrderResponse getOrderById(Integer orderId);

    OrderResponse createOrder(OrderRequest request);

    OrderResponse updateOrder(Integer orderId, OrderRequest request);

    void deleteOrder(Integer idOrder);
}
