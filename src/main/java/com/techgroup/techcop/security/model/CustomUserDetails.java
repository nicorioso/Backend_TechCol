package com.techgroup.techcop.security.model;

import com.techgroup.techcop.domain.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {

    private final Customer customer;

    public CustomUserDetails(Customer customer) {
        this.customer = customer;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Si manejas roles, puedes mapearlos aquí
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return customer.getCustomerPassword();
    }

    @Override
    public String getUsername() {
        return customer.getCustomerEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    public Customer getCustomer() {
        return customer;
    }
}
