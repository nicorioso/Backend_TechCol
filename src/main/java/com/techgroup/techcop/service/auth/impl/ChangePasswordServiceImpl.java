package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.service.auth.ChangePasswordService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final VerificationCodeService verificationCodeService;

    public ChangePasswordServiceImpl(CustomerRepository customerRepository, PasswordEncoder passwordEncoder, AuthenticationManager authManager, VerificationCodeService verificationCodeService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.verificationCodeService = verificationCodeService;
    }

    @Override
    public String changePasswordAuthenticate(String email, String password, String channel) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        Customer customer = getCustomerByEmail(email);

        verificationCodeService.generateAndSendCode(
                customer,
                VerificationChannel.valueOf(channel.toUpperCase()),
                VerificationPurpose.CHANGE_PASSWORD
        );

        return "Verification code sent";
    }

    @Override
    public String changePasswordVerifiCode(String email, String code) {
        Customer customer = getCustomerByEmail(email);

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new RuntimeException("Invalid code");
        }

        customer.setPasswordRsetVerified(true);
        customerRepository.save(customer);

        return "successful verification";
    }

    @Override
    public String changePassword(String email, String newPassword) {
        Customer customer = getCustomerByEmail(email);

        if (customer.isPasswordRsetVerified() != false) {
            customer.setCustomerPassword(passwordEncoder.encode(newPassword));
            customer.setPasswordRsetVerified(false);
            customerRepository.save(customer);
            return "Change password successful";
        }else {
            throw new RuntimeException("Verification required");
        }
    }

    private Customer getCustomerByEmail(String email) {
        return customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}