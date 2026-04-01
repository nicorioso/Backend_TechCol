package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.OrderDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailsRepository extends JpaRepository<OrderDetails, Integer> {
    @Query("select count(od) > 0 from OrderDetails od where od.product.product_id = :productId")
    boolean existsByProductId(@Param("productId") Integer productId);
}
