package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.jwt.JwtService;
import com.techgroup.techcop.security.model.CustomUserDetails;
import com.techgroup.techcop.service.auth.AuthenticationService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final CustomerRepository customerRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final VerificationCodeService verificationCodeService;


    public AuthenticationServiceImpl(CustomerRepository customerRepository, JwtService jwtService, AuthenticationManager authManager, VerificationCodeService verificationCodeService) {
        this.customerRepository = customerRepository;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public String login(String email, String password) {

        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        verificationCodeService.generateAndSendCode(customer);

        return "Verification code sent to email";
    }

    @Override
    public String verifyCode(String email, String code) {

        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new RuntimeException("Invalid code");
        }

        return jwtService.generateToken(new CustomUserDetails(customer));
    }


}