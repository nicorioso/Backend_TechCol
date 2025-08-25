package com.techgroup.techcop.controllers;

import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/Customer")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        return ResponseEntity.ok(customerService.getCustomer());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCustomerById(@PathVariable Integer id) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);

        return customerOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> postCustomer(@RequestBody Customer customer) {
        Customer nuevo = customerService.createCustomer(customer);
        URI location = URI.create("/Customer/" + nuevo.getCustomer_id());
        return ResponseEntity.created(location).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody Customer updatedCustomer) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (customerOpt.isPresent()) {
            Customer existingCustomer = customerOpt.get();
            existingCustomer.setName(updatedCustomer.getName());
            existingCustomer.setLast_name(updatedCustomer.getLast_name());
            existingCustomer.setEmail(updatedCustomer.getEmail());
            existingCustomer.setUser_password(updatedCustomer.getUser_password());
            existingCustomer.setPhone_number(updatedCustomer.getPhone_number());
            existingCustomer.setRole_id(updatedCustomer.getRole_id());
            // Agrega más campos si los tienes en tu entidad

            Customer saved = customerService.createCustomer(existingCustomer); // reutilizamos createCustomer para guardar
            return ResponseEntity.ok(saved);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchCustomer(@PathVariable Integer id, @RequestBody Customer customer) {
        try {
            Customer patch = customerService.patchCustomer(id, customer);
            return ResponseEntity.ok(patch);
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe el producto con el id " + customer.getCustomer_id());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable Integer id) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (customerOpt.isPresent()) {
            customerService.deleteCustomer(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
