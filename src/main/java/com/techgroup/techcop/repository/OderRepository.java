package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OderRepository extends JpaRepository<Orders, Integer> {

}
