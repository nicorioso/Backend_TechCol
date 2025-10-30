package com.techgroup.techcop.security.model;

import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.repository.CustomerDBA;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private CustomerDBA customerDBA;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Customer customer = (Customer) customerDBA.findByCustomerEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
        return new CustomUserDetails(customer);
    }
}

