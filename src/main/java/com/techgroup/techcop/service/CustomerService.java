package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.Customer;
import java.util.List;
import java.util.Optional;

public interface CustomerService {

    public List<Customer> getCustomer();
    Optional<Customer> getCustomerById(Integer id);
    public Customer createCustomer(Customer customer);
    Customer updateCustomer(Integer id, Customer customer);
    void deleteCustomer(Integer id);
    public Customer patchCustomer(Integer id, Customer customer);
}
