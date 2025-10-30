package com.techgroup.techcop.service;

import com.techgroup.techcop.domain.Customer;
import com.techgroup.techcop.repository.CustomerDBA;
import com.techgroup.techcop.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerDBA customerDBA;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        UserDetails user = (UserDetails) customerDBA.findByCustomerEmail(email).get();
        return jwtService.generateToken(user);
    }

    public Customer register(Customer customer) {
        customer.setCustomerPassword(passwordEncoder.encode(customer.getCustomerPassword()));
        return customerDBA.save(customer);
    }
}

