package com.techgroup.techcop.service.order.impl;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.service.order.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Orders> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public List<Orders> getOrdersByCustomerId(Integer id) {
        return orderRepository.findAllByCustomerCustomerId(id);
    }

    @Override
    public void deleteOrder(Integer idOrder) {
        Orders orders = orderRepository.findById(idOrder)
                .orElseThrow(() -> new RuntimeException("No existe la orden con id " + idOrder));
        orderRepository.delete(orders);
    }
}
