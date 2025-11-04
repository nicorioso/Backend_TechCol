package com.techgroup.techcop.service;


import com.techgroup.techcop.domain.Carts;
import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.repository.CartsDBA;
import com.techgroup.techcop.repository.CustomerDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerDBA customerDBA;

    @Autowired
    private CartsDBA cartsDBA;

    @Override
    public List<Customer> getCustomer() {
        return customerDBA.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(Integer id) {
        Optional<Customer> customer = customerDBA.findById(id);
        if (customer.isPresent()) {
            return customer;
        }else {
            return Optional.empty();
        }
    }

    @Override
    public Customer createCustomer(Customer customer) {
        customerDBA.save(customer);

        Carts cart = new Carts();
        cart.setCustomer(customer);
        cart.setCart_price(0.0);
        cart.setCreate_at(LocalDateTime.now());

        cartsDBA.save(cart);

        return customer;
    }

    @Override
    public Customer updateCustomer(Integer id, Customer customer) {
        return customerDBA.findById(id).map(existing -> {
            existing.setCustomerName(customer.getCustomerName());
            existing.setCustomerLastName(customer.getCustomerLastName());
            existing.setCustomerEmail(customer.getCustomerEmail());
            existing.setCustomerPassword(customer.getCustomerPassword());
            existing.setCustomerPhoneNumber(customer.getCustomerPhoneNumber());
            existing.setRoleId(customer.getRoleId());
            return customerDBA.save(existing);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    @Override
    public Customer patchCustomer(Integer id, Customer customer) {
        Customer customerExist = customerDBA.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        if (customer.getCustomerName() != null) {
            customerExist.setCustomerName(customer.getCustomerName());
        }
        if (customer.getCustomerLastName() != null) {
            customerExist.setCustomerLastName(customer.getCustomerLastName());
        }
        if (customer.getCustomerEmail() != null) {
            customerExist.setCustomerEmail(customer.getCustomerEmail());
        }
        if (customer.getCustomerPassword() != null) {
            customerExist.setCustomerPassword(customer.getCustomerPassword());
        }
        if (customer.getCustomerPhoneNumber() != null) {
            customerExist.setCustomerPhoneNumber(customer.getCustomerPhoneNumber());
        }
        if (customer.getRoleId() != null) {
            customerExist.setRoleId(customer.getRoleId());
        }
        return customerDBA.save(customerExist);
    }

    public void deleteCustomer(Integer id) {
        customerDBA.deleteById(id);
    }
}
