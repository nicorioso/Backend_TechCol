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
@RequestMapping("/customers")
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

    @PostMapping("/register")  // ← AGREGAR: endpoint de registro
    public ResponseEntity<?> registerCustomer(@RequestBody Customer customer) {
        try {
            Customer nuevo = customerService.createCustomer(customer);
            URI location = URI.create("/customers/" + nuevo.getCustomerId());
            return ResponseEntity.created(location).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<?> postCustomer( @RequestBody Customer customer) {
        try {
            Customer nuevo = customerService.createCustomer(customer);
            URI location = URI.create("/customers/" + nuevo.getCustomerId());
            return ResponseEntity.created(location).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id,@RequestBody Customer updatedCustomer) {
        Optional<Customer> customerOpt = customerService.getCustomerById(id);
        if (customerOpt.isPresent()) {
            Customer existingCustomer = customerOpt.get();
            existingCustomer.setCustomerName(updatedCustomer.getCustomerName());
            existingCustomer.setCustomerLastName(updatedCustomer.getCustomerLastName());
            existingCustomer.setCustomerEmail(updatedCustomer.getCustomerEmail());
            existingCustomer.setCustomerPassword(updatedCustomer.getCustomerPassword());
            existingCustomer.setCustomerPhoneNumber(updatedCustomer.getCustomerPhoneNumber());
            existingCustomer.setRoleId(updatedCustomer.getRoleId());

            Customer saved = customerService.createCustomer(existingCustomer);
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
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No existe el cliente con el id " + id);
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
