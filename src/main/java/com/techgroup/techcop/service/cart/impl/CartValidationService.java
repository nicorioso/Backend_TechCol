package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import org.springframework.stereotype.Service;

@Service
public class CartValidationService {

    private final CustomerRepository customerRepository;

    public CartValidationService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Carts validateCustomerCart(Integer customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer.getCart() == null) {
            Carts cart = new Carts();
            cart.setCustomer(customer);
            cart.setCart_price(0.0);
            customer.setCart(cart);
            return cart;
        }
        return customer.getCart();
    }

}
