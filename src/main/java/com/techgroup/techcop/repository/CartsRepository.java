package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartsRepository extends JpaRepository<Carts, Integer> {
    Optional<Carts> findByCustomer(Customer customer);
}