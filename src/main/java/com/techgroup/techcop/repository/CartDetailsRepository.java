package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailsRepository extends JpaRepository<CartItem, Integer> {
}
