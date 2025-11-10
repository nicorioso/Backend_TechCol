package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.Carts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartsDBA extends JpaRepository<Carts, Integer> {
}