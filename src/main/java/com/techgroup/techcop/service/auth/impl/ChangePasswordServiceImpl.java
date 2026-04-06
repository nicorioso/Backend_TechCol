package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.audit.AuditLogService;
import com.techgroup.techcop.service.auth.ChangePasswordService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ChangePasswordServiceImpl implements ChangePasswordService {

    private final CustomerRepository customerRepository;
    private final PasswordHashingService passwordHashingService;
    private final VerificationCodeService verificationCodeService;
    private final AuditLogService auditLogService;

    public ChangePasswordServiceImpl(CustomerRepository customerRepository,
                                     PasswordHashingService passwordHashingService,
                                     VerificationCodeService verificationCodeService,
                                     AuditLogService auditLogService) {
        this.customerRepository = customerRepository;
        this.passwordHashingService = passwordHashingService;
        this.verificationCodeService = verificationCodeService;
        this.auditLogService = auditLogService;
    }

    @Override
    public String changePasswordAuthenticate(String email, String password, String channel) {
        email = normalizeEmail(email);
        Customer customer = getCustomerByEmail(email);
        ensureCurrentPasswordMatches(password, customer);
        upgradeLegacyPasswordHash(customer);
        VerificationChannel verificationChannel = parseChannel(channel);

        verificationCodeService.generateAndSendCode(
                customer,
                verificationChannel,
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }

        customer.setPasswordRsetVerified(true);
        customerRepository.save(customer);

        return "successful verification";
    }

    @Override
    public String changePassword(String email, String newPassword) {
        email = normalizeEmail(email);
        Customer customer = getCustomerByEmail(email);

        if (customer.isPasswordRsetVerified()) {
            customer.setCustomerPassword(passwordHashingService.hashNewPassword(newPassword));
            customer.setPasswordRsetVerified(false);
            customerRepository.save(customer);
            auditLogService.log(
                    customer.getCustomerEmail(),
                    "PASSWORD_CHANGED",
                    "CUSTOMER",
                    customer.getCustomerId() == null ? null : customer.getCustomerId().toString(),
                    "Cambio de contrasena autenticado por el usuario"
            );
            return "Change password successful";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verification required");
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
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
            customer.setCustomerPassword(passwordHashingService.hashNewPassword(newPassword));
            customer.setPasswordRsetVerified(false);
            customerRepository.save(customer);
            auditLogService.log(
                    customer.getCustomerEmail(),
                    "PASSWORD_RECOVERY_RESET",
                    "CUSTOMER",
                    customer.getCustomerId() == null ? null : customer.getCustomerId().toString(),
                    "Contrasena restablecida por flujo de recuperacion"
            );
            return "Change password successful";
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Verification required");
        }
    }

    private void ensureCurrentPasswordMatches(String rawPassword, Customer customer) {
        if (!passwordHashingService.matches(rawPassword, customer.getCustomerPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
        }
    }

    private void upgradeLegacyPasswordHash(Customer customer) {
        String storedPassword = customer.getCustomerPassword();
        String encodedPassword = passwordHashingService.encodeIfNeeded(storedPassword);

        if (storedPassword != null && !storedPassword.equals(encodedPassword)) {
            customer.setCustomerPassword(encodedPassword);
            customerRepository.save(customer);
        }
    }

    private Customer getCustomerByEmail(String email) {
        return customerRepository
                .findByCustomerEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Customer getCustomerByIdentifier(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.findByCustomerEmail(normalizedIdentifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            case SMS -> customerRepository.findByCustomerPhoneNumber(normalizedIdentifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        };
    }

    private VerificationChannel parseChannel(String channel) {
        if (channel == null || channel.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Verification channel is required");
        }

        try {
            return VerificationChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification channel", ex);
        }
    }

    private String normalizeIdentifier(String identifier, VerificationChannel channel) {
        String value = identifier == null ? "" : identifier.trim();

        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifier is required");
        }

        return channel == VerificationChannel.EMAIL ? normalizeEmail(value) : value;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
