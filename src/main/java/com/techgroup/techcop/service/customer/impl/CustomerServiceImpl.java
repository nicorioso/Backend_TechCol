package com.techgroup.techcop.service.customer.impl;

import com.techgroup.techcop.model.dto.CustomerProfileUpdateRequest;
import com.techgroup.techcop.model.dto.CustomerResponse;
import com.techgroup.techcop.model.dto.CustomerRoleUpdateRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Role;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.access.ResourceAuthorizationService;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
import com.techgroup.techcop.service.customer.CustomerService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final RoleRepository roleRepository;
    private final ResourceAuthorizationService resourceAuthorizationService;
    private final PasswordHashingService passwordHashingService;
    private final AuthIdentityService authIdentityService;

    public CustomerServiceImpl(CustomerRepository customerRepository,
                               RoleRepository roleRepository,
                               ResourceAuthorizationService resourceAuthorizationService,
                               PasswordHashingService passwordHashingService,
                               AuthIdentityService authIdentityService) {
        this.customerRepository = customerRepository;
        this.roleRepository = roleRepository;
        this.resourceAuthorizationService = resourceAuthorizationService;
        this.passwordHashingService = passwordHashingService;
        this.authIdentityService = authIdentityService;
    }

    @Override
    public List<CustomerResponse> getCustomer() {
        return customerRepository.findAll().stream()
                .map(CustomerResponse::fromEntity)
                .toList();
    }

    @Override
    public Optional<CustomerResponse> getCustomerById(Integer id) {
        resourceAuthorizationService.assertCurrentCustomer(id);
        return customerRepository.findById(id)
                .map(CustomerResponse::fromEntity);
    }

    @Override
    public CustomerResponse updateCustomer(Integer id, CustomerProfileUpdateRequest customer) {
        return saveProfileChanges(id, customer);
    }

    @Override
    public CustomerResponse patchCustomer(Integer id, CustomerProfileUpdateRequest customer) {
        return saveProfileChanges(id, customer);
    }

    @Override
    public CustomerResponse updateRole(Integer id, CustomerRoleUpdateRequest request) {
        if (!resourceAuthorizationService.isAdmin()) {
            throw new AccessDeniedException("Only admins can change roles");
        }

        Customer customer = getCustomerEntity(id);
        String normalizedRole = normalizeRoleName(request.getRole());
        Role role = roleRepository.findByRoleName(normalizedRole)
                .orElseThrow(() -> new ResponseStatusException(BAD_REQUEST, "Role not found: " + normalizedRole));

        customer.setRole(role);
        return CustomerResponse.fromEntity(saveCustomer(customer));
    }

    @Override
    public void deleteCustomer(Integer id) {
        if (!resourceAuthorizationService.isAdmin()) {
            throw new AccessDeniedException("Only admins can delete customers");
        }

        Customer customer = getCustomerEntity(id);
        customerRepository.delete(customer);
    }

    private CustomerResponse saveProfileChanges(Integer id, CustomerProfileUpdateRequest request) {
        resourceAuthorizationService.assertCurrentCustomer(id);
        Customer existing = getCustomerEntity(id);

        String normalizedEmail = normalizeEmail(request.getCustomerEmail());
        if (normalizedEmail.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Email is required");
        }

        customerRepository.findByCustomerEmail(normalizedEmail)
                .filter(other -> !other.getCustomerId().equals(id))
                .ifPresent(other -> {
                    throw new ResponseStatusException(BAD_REQUEST, "Email is already in use");
                });

        String normalizedPhone = normalizePhone(request.getCustomerPhoneNumber());
        if (normalizedPhone != null) {
            authIdentityService.findCustomerByIdentifier(normalizedPhone, VerificationChannel.SMS)
                    .filter(other -> !other.getCustomerId().equals(id))
                    .ifPresent(other -> {
                        throw new ResponseStatusException(BAD_REQUEST, "Phone is already in use");
                    });
        }

        existing.setCustomerName(trimToEmpty(request.getCustomerName()));
        existing.setCustomerLastName(trimToEmpty(request.getCustomerLastName()));
        existing.setCustomerEmail(normalizedEmail);
        existing.setCustomerPhoneNumber(normalizedPhone);

        return CustomerResponse.fromEntity(saveCustomer(existing));
    }

    private Customer saveCustomer(Customer customer) {
        customer.setCustomerPassword(passwordHashingService.encodeIfNeeded(customer.getCustomerPassword()));
        return customerRepository.save(customer);
    }

    private Customer getCustomerEntity(Integer id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
    }

    private String normalizeRoleName(String role) {
        String normalized = trimToEmpty(role).toUpperCase();
        if (normalized.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Role is required");
        }
        return normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
    }

    private String normalizeEmail(String email) {
        return trimToEmpty(email).toLowerCase();
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToNull(String value) {
        String trimmed = trimToEmpty(value);
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePhone(String phone) {
        String trimmed = trimToEmpty(phone);
        if (trimmed.isEmpty()) {
            return null;
        }

        return authIdentityService.normalizePhone(trimmed);
    }
}
