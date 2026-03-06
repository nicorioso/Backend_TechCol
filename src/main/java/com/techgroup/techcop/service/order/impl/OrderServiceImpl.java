package com.techgroup.techcop.service.order.impl;

import com.techgroup.techcop.model.dto.OrderRequest;
import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.service.order.OrderService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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
    public Optional<Orders> getOrdersByIdCustomer(Integer id) {
        Optional<Orders> orders = orderRepository.findById(id);
        if (orders.isPresent()){
            return orders;
        }else {
            return Optional.empty();
        }
    }

    @Override
    public OrderRequest updateOrder(Integer id, Orders order) {
        return null;
    }

    @Override
    public void deleteOrder(Integer idCustomer, Integer idOrder) {

    }
}
