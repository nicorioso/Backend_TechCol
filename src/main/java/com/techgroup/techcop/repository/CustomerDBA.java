package com.techgroup.techcop.repository;

import com.techgroup.techcop.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerDBA extends JpaRepository<Customer, Integer> {

    Optional<Object> findByCustomerEmail(String email);
}
