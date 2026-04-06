package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.dto.RegisterCustomerRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.auth.RegistrationService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final CustomerRepository customerRepository;
    private final PasswordHashingService passwordHashingService;
    private final RoleRepository roleRepository;
    private final VerificationCodeService verificationCodeService;

    public RegistrationServiceImpl(CustomerRepository customerRepository,
                                   PasswordHashingService passwordHashingService,
                                   RoleRepository roleRepository,
                                   VerificationCodeService verificationCodeService) {
        this.customerRepository = customerRepository;
        this.passwordHashingService = passwordHashingService;
        this.roleRepository = roleRepository;
        this.verificationCodeService = verificationCodeService;
    }

    public String registerRequest(RegisterCustomerRequest request) {
        String normalizedEmail = request.getCustomerEmail() == null
                ? ""
                : request.getCustomerEmail().trim().toLowerCase();

        if (normalizedEmail.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El correo es obligatorio");
        }

        String rawPassword = request.getCustomerPassword() == null
                ? ""
                : request.getCustomerPassword().trim();

        if (rawPassword.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contrasena es obligatoria");
        }

        if (customerRepository.existsByCustomerEmail(normalizedEmail)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ya existe una cuenta registrada con ese correo"
            );
        }

        Customer customer = new Customer();
        customer.setCustomerName(trimToEmpty(request.getCustomerName()));
        customer.setCustomerLastName(trimToEmpty(request.getCustomerLastName()));
        customer.setCustomerEmail(normalizedEmail);
        customer.setCustomerPhoneNumber(trimToEmpty(request.getCustomerPhoneNumber()));
        customer.setCustomerPassword(passwordHashingService.hashNewPassword(rawPassword));
        customer.setRole(roleRepository.findByRoleName("ROLE_CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo resolver el rol por defecto"
                )));

        customerRepository.save(customer);

        verificationCodeService.generateAndSendCode(
                customer,
                VerificationChannel.EMAIL,
                VerificationPurpose.REGISTER
        );

        return "Verification code sent";
    }

    public String verifyRegister(String email, String code) {
        email = email == null ? "" : email.trim().toLowerCase();

        Customer customer = customerRepository.findByCustomerEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean valid = verificationCodeService.verifyCode(customer, code);

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }

        customerRepository.save(customer);

        return "Account verified successfully";
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
