package com.techgroup.techcop.service.order;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.entity.Orders;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    public List<Orders> getAllOrders();
    Optional<Orders> getOrdersByIdCustomer(Integer id);
    OrderRequest updateOrder(Integer id, Orders order);
    void deleteOrder(Integer idCustomer, Integer idOrder);
}
