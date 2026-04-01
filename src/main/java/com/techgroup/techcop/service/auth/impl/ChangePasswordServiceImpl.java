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
        email = normalizeEmail(email);
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
        email = normalizeEmail(email);
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
        email = normalizeEmail(email);
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

    @Override
    public String requestPasswordRecovery(String identifier, String channel) {
        VerificationChannel verificationChannel = parseChannel(channel);
        Customer customer = getCustomerByIdentifier(identifier, verificationChannel);

        verificationCodeService.generateAndSendCode(
                customer,
                verificationChannel,
                VerificationPurpose.CHANGE_PASSWORD
        );

        return "Verification code sent";
    }

    @Override
    public String verifyPasswordRecoveryCode(String identifier, String channel, String code) {
        VerificationChannel verificationChannel = parseChannel(channel);
        Customer customer = getCustomerByIdentifier(identifier, verificationChannel);

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new RuntimeException("Invalid code");
        }

        customer.setPasswordRsetVerified(true);
        customerRepository.save(customer);

        return "successful verification";
    }

    @Override
    public String resetPasswordByRecovery(String identifier, String channel, String newPassword) {
        VerificationChannel verificationChannel = parseChannel(channel);
        Customer customer = getCustomerByIdentifier(identifier, verificationChannel);

        if (customer.isPasswordRsetVerified()) {
            customer.setCustomerPassword(passwordEncoder.encode(newPassword));
            customer.setPasswordRsetVerified(false);
            customerRepository.save(customer);
            return "Change password successful";
        } else {
            throw new RuntimeException("Verification required");
        }
    }

    private Customer getCustomerByEmail(String email) {
        return customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Customer getCustomerByIdentifier(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.findByCustomerEmail(normalizedIdentifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            case SMS -> customerRepository.findByCustomerPhoneNumber(normalizedIdentifier)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        };
    }

    private VerificationChannel parseChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new RuntimeException("Verification channel is required");
        }

        return VerificationChannel.valueOf(channel.trim().toUpperCase());
    }

    private String normalizeIdentifier(String identifier, VerificationChannel channel) {
        String value = identifier == null ? "" : identifier.trim();

        if (value.isEmpty()) {
            throw new RuntimeException("Identifier is required");
        }

        return channel == VerificationChannel.EMAIL ? normalizeEmail(value) : value;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
