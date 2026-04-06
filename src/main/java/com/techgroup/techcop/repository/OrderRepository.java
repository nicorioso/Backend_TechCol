package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Orders;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Integer> {

    @Override
    @EntityGraph(attributePaths = {"customer", "orderDetails", "orderDetails.product"})
    List<Orders> findAll();

    @Override
    @EntityGraph(attributePaths = {"customer", "orderDetails", "orderDetails.product"})
    Optional<Orders> findById(Integer integer);

    Optional<Orders> findByPaypalOrderId(String paypalOrderId);

    @EntityGraph(attributePaths = {"customer", "orderDetails", "orderDetails.product"})
    List<Orders> findAllByCustomerCustomerId(Integer customerId);
}
