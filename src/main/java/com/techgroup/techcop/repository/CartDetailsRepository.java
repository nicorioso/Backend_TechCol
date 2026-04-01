package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartDetailsRepository extends JpaRepository<CartItem, Integer> {
    @Query("select count(cd) > 0 from CartItem cd where cd.product_id = :productId")
    boolean existsByProductId(@Param("productId") Integer productId);
}
