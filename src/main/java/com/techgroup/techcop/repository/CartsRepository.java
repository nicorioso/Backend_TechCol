package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartsRepository extends JpaRepository<Carts, Integer> {
}