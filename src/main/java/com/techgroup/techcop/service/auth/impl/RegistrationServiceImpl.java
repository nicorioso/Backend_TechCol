package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.service.auth.RegistrationService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final VerificationCodeService verificationCodeService;

    public RegistrationServiceImpl(CustomerRepository customerRepository,
                                   PasswordEncoder passwordEncoder,
                                   RoleRepository roleRepository,
                                   VerificationCodeService verificationCodeService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.verificationCodeService = verificationCodeService;
    }

    public String registerRequest(Customer customer) {

        customer.setCustomerPassword(passwordEncoder.encode(customer.getCustomerPassword()));
        customer.setRole(roleRepository.findByRoleName("ROLE_CLIENTE")
                .orElseThrow());

        customerRepository.save(customer);

        verificationCodeService.generateAndSendCode(
                customer,
                VerificationChannel.EMAIL,
                VerificationPurpose.REGISTER
        );

        return "Verification code sent";
    }

    public String verifyRegister(String email, String code) {

        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow();

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new RuntimeException("Invalid code");
        }

        customerRepository.save(customer);

        return "Account verified successfully";
    }
}