package com.techgroup.techcop.repository;

import com.techgroup.techcop.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {

    Optional<Customer> findByCustomerEmail(String email);

    Optional<Customer> findByCustomerPhoneNumber(String customerPhoneNumber);

    boolean existsByCustomerEmail(String email);
}
