package com.techgroup.techcop.service.cart.impl;

import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.access.ResourceAuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CartValidationService {

    private final CustomerRepository customerRepository;
    private final ResourceAuthorizationService resourceAuthorizationService;

    public CartValidationService(CustomerRepository customerRepository,
                                 ResourceAuthorizationService resourceAuthorizationService) {
        this.customerRepository = customerRepository;
        this.resourceAuthorizationService = resourceAuthorizationService;
    }

    public Carts validateCustomerCart(Integer customerId) {
        resourceAuthorizationService.assertCurrentCustomer(customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        if (customer.getCart() == null) {
            Carts cart = new Carts();
            cart.setCustomer(customer);
            cart.setCart_price(BigDecimal.ZERO);
            customer.setCart(cart);
            return cart;
        }

        return customer.getCart();
    }
}
