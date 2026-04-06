package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartsRepository extends JpaRepository<Carts, Integer> {

    @Override
    @EntityGraph(attributePaths = {"customer", "items"})
    List<Carts> findAll();

    Optional<Carts> findByCustomer(Customer customer);
}
