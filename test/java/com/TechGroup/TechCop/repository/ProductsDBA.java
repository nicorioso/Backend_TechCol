package com.techgroup.techcop.repository;

import com.techgroup.techcop.domain.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductsDBA extends JpaRepository<Products, Integer> {
}