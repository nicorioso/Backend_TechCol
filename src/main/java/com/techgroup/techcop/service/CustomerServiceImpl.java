package com.techgroup.techcop.service;


import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.repository.CustomerDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService{

    @Autowired
    private CustomerDBA customerDBA;

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
        return customerDBA.save(customer);
    }

    @Override
    public Customer updateCustomer(Integer id, Customer customer) {
        return customerDBA.findById(id).map(existing -> {
            existing.setName(customer.getName());
            existing.setLast_name(customer.getLast_name());
            existing.setEmail(customer.getEmail());
            existing.setUser_password(customer.getUser_password());
            existing.setPhone_number(customer.getPhone_number());
            existing.setRole_id(customer.getRole_id());
            return customerDBA.save(existing);
        }).orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + id));
    }

    @Override
    public Customer patchCustomer(Integer id, Customer customer) {
        Customer customerExist = customerDBA.findById(id)
                .orElseThrow(() -> new RuntimeException("No existe el producto con el id: " + id));
        if (customer.getName() != null) {
            customerExist.setName(customer.getName());
        }
        if (customer.getLast_name() != null) {
            customerExist.setLast_name(customer.getLast_name());
        }
        if (customer.getEmail() != null) {
            customerExist.setEmail(customer.getEmail());
        }
        if (customer.getUser_password() != null) {
            customerExist.setUser_password(customer.getUser_password());
        }
        if (customer.getPhone_number() != null) {
            customerExist.setPhone_number(customer.getPhone_number());
        }
        if (customer.getRole_id() != null) {
            customerExist.setRole_id(customer.getRole_id());
        }
        return customerDBA.save(customerExist);
    }

    public void deleteCustomer(Integer id) {
        customerDBA.deleteById(id);
    }
}
