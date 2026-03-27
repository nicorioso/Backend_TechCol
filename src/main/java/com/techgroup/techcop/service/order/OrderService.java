package com.techgroup.techcop.service.order;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.entity.Orders;

import java.util.List;

public interface OrderService {
    public List<Orders> getAllOrders();
    List<Orders> getOrdersByCustomerId(Integer id);
    void deleteOrder(Integer idOrder);
}
