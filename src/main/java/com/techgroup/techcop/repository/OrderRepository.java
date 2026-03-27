package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders, Integer> {
    Optional<Orders> findByPaypalOrderId(String paypalOrderId);
}
