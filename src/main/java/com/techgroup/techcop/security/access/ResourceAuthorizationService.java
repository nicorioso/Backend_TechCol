package com.techgroup.techcop.security.access;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.Orders;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.OrderRepository;
import com.techgroup.techcop.security.model.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Service("resourceAuthorizationService")
public class ResourceAuthorizationService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public ResourceAuthorizationService(CustomerRepository customerRepository,
                                        OrderRepository orderRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    public Integer getAuthenticatedCustomerId() {
        Authentication authentication = getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getId();
        }

        String email = extractPrincipalEmail(authentication);
        return customerRepository.findByCustomerEmail(email)
                .map(Customer::getCustomerId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authenticated user was not found"));
    }

    public Customer getAuthenticatedCustomer() {
        Integer customerId = getAuthenticatedCustomerId();
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new ResponseStatusException(UNAUTHORIZED, "Authenticated user was not found"));
    }

    public boolean isAdmin() {
        return getAuthentication().getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }

    public boolean isCurrentCustomer(Integer customerId) {
        return customerId != null && (isAdmin() || Objects.equals(getAuthenticatedCustomerId(), customerId));
    }

    public boolean canAccessOrder(Integer orderId) {
        if (orderId == null) {
            return false;
        }

        if (isAdmin()) {
            return true;
        }

        Integer authenticatedCustomerId = getAuthenticatedCustomerId();
        return orderRepository.findById(orderId)
                .map(order -> belongsToCustomer(order, authenticatedCustomerId))
                .orElse(false);
    }

    public void assertCurrentCustomer(Integer customerId) {
        if (!isCurrentCustomer(customerId)) {
            throw new AccessDeniedException("You cannot access another customer's resources");
        }
    }

    public void assertCanAccessOrder(Orders order) {
        if (order == null) {
            throw new ResponseStatusException(NOT_FOUND, "Order not found");
        }

        if (!isAdmin() && !belongsToCustomer(order, getAuthenticatedCustomerId())) {
            throw new AccessDeniedException("You cannot access another customer's order");
        }
    }

    private boolean belongsToCustomer(Orders order, Integer customerId) {
        return order.getCustomer() != null
                && order.getCustomer().getCustomerId() != null
                && Objects.equals(order.getCustomer().getCustomerId(), customerId);
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new ResponseStatusException(UNAUTHORIZED, "Authentication is required");
        }
        return authentication;
    }

    private String extractPrincipalEmail(Authentication authentication) {
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        }

        return authentication.getName();
    }
}
