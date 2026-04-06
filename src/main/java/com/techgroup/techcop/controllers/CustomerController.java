package com.techgroup.techcop.controllers;

import com.techgroup.techcop.model.dto.CustomerProfileUpdateRequest;
import com.techgroup.techcop.model.dto.CustomerResponse;
import com.techgroup.techcop.model.dto.CustomerRoleUpdateRequest;
import com.techgroup.techcop.service.customer.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@Validated
@RequestMapping("/customers")
@Tag(name = "Customers", description = "Gestion de perfiles de cliente y administracion de roles")
@SecurityRequirement(name = "bearerAuth")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Listar todos los clientes")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<java.util.List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getCustomer());
    }

    @Operation(summary = "Obtener un cliente por id")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable @Positive(message = "El id debe ser mayor que cero") Integer id) {
        Optional<CustomerResponse> customerOpt = customerService.getCustomerById(id);
        return customerOpt
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
    }

    @Operation(summary = "Actualizar el perfil de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable @Positive(message = "El id debe ser mayor que cero") Integer id,
                                                           @Valid @RequestBody CustomerProfileUpdateRequest updatedCustomer) {
        CustomerResponse saved = customerService.updateCustomer(id, updatedCustomer);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Modificar parcialmente el perfil de un cliente")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @PatchMapping("/{id}")
    public ResponseEntity<CustomerResponse> patchCustomer(@PathVariable @Positive(message = "El id debe ser mayor que cero") Integer id,
                                                          @Valid @RequestBody CustomerProfileUpdateRequest customer) {
        return ResponseEntity.ok(customerService.patchCustomer(id, customer));
    }

    @Operation(summary = "Actualizar el rol de un cliente")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    public ResponseEntity<CustomerResponse> updateCustomerRole(@PathVariable @Positive(message = "El id debe ser mayor que cero") Integer id,
                                                               @Valid @RequestBody CustomerRoleUpdateRequest request) {
        return ResponseEntity.ok(customerService.updateRole(id, request));
    }

    @Operation(summary = "Eliminar un cliente")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCustomer(@PathVariable @Positive(message = "El id debe ser mayor que cero") Integer id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
