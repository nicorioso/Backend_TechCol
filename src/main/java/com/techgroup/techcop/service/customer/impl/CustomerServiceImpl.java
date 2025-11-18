package com.techgroup.techcop.service.customer.impl;


import com.techgroup.techcop.model.entity.Carts;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CartsRepository;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.service.customer.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CartsRepository cartsRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository, CartsRepository cartsRepository) {
        this.customerRepository = customerRepository;
        this.cartsRepository = cartsRepository;
    }

    @Override
    public List<Customer> getCustomer() {
        return customerRepository.findAll();
    }

    @Override
    public Optional<Customer> getCustomerById(Integer id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            return customer;
        }else {
            return Optional.empty();
        }
    }

    @Override
    public Customer updateCustomer(Integer id, Customer customer) {
        return customerRepository.findById(id).map(existing -> {
            existing.setCustomerName(customer.getCustomerName());
            existing.setCustomerLastName(customer.getCustomerLastName());
            existing.setCustomerEmail(customer.getCustomerEmail());
            existing.setCustomerPassword(customer.getCustomerPassword());
            existing.setCustomerPhoneNumber(customer.getCustomerPhoneNumber());
            existing.setRoleId(customer.getRoleId());
            return customerRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    @Override
    public Customer patchCustomer(Integer id, Customer customer) {
        Customer customerExist = customerRepository.findById(id)
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
        return customerRepository.save(customerExist);
    }

    public void deleteCustomer(Integer id) {
        customerRepository.deleteById(id);
    }
}
