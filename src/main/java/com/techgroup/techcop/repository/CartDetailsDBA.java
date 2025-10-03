package com.techgroup.techcop.repository;

import com.techgroup.techcop.domain.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartDetailsDBA extends JpaRepository<CartItem, Integer> {
}
