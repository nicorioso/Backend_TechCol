package com.techgroup.techcop.repository;

import com.techgroup.techcop.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerDBA extends JpaRepository<Customer, Integer> {

}
