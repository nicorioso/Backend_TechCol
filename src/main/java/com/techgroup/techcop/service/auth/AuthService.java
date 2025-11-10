package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.model.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String login(String email, String password) {

        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        Customer customer = (Customer) customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        CustomUserDetails userDetails = new CustomUserDetails(customer);

        return jwtService.generateToken(userDetails);
    }

    public Customer register(Customer customer) {
        customer.setCustomerPassword(passwordEncoder.encode(customer.getCustomerPassword()));
        return customerRepository.save(customer);
    }
}


