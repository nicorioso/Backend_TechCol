package com.techgroup.techcop.service.auth.impl;

import com.techgroup.techcop.model.dto.RegisterCustomerRequest;
import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.repository.RoleRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.security.password.PasswordHashingService;
import com.techgroup.techcop.service.auth.RegistrationService;
import com.techgroup.techcop.service.auth.support.AuthIdentityService;
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
    private final AuthIdentityService authIdentityService;

    public RegistrationServiceImpl(CustomerRepository customerRepository,
                                   PasswordHashingService passwordHashingService,
                                   RoleRepository roleRepository,
                                   VerificationCodeService verificationCodeService,
                                   AuthIdentityService authIdentityService) {
        this.customerRepository = customerRepository;
        this.passwordHashingService = passwordHashingService;
        this.roleRepository = roleRepository;
        this.verificationCodeService = verificationCodeService;
        this.authIdentityService = authIdentityService;
    }

    @Override
    public String registerRequest(RegisterCustomerRequest request) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(
                request.getChannel(),
                VerificationChannel.EMAIL
        );
        String normalizedEmail = authIdentityService.normalizeEmail(request.getCustomerEmail());

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

        String normalizedPhone = trimToEmpty(request.getCustomerPhoneNumber());
        if (verificationChannel == VerificationChannel.SMS && normalizedPhone.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El telefono es obligatorio cuando el canal de verificacion es SMS"
            );
        }

        if (!normalizedPhone.isEmpty()) {
            normalizedPhone = authIdentityService.normalizePhone(normalizedPhone);
            if (authIdentityService.accountExists(normalizedPhone, VerificationChannel.SMS)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Ya existe una cuenta registrada con ese telefono"
                );
            }
        }

        Customer customer = new Customer();
        customer.setCustomerName(trimToEmpty(request.getCustomerName()));
        customer.setCustomerLastName(trimToEmpty(request.getCustomerLastName()));
        customer.setCustomerEmail(normalizedEmail);
        customer.setCustomerPhoneNumber(normalizedPhone);
        customer.setCustomerPassword(passwordHashingService.hashNewPassword(rawPassword));
        customer.setAccountVerified(false);
        customer.setRole(roleRepository.findByRoleName("ROLE_CLIENTE")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se pudo resolver el rol por defecto"
                )));

        customerRepository.save(customer);

        verificationCodeService.generateAndSendCode(
                customer,
                verificationChannel,
                VerificationPurpose.REGISTER
        );

        return "Verification code sent";
    }

    @Override
    public String resendRegisterCode(String identifier, String channel) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(channel, VerificationChannel.EMAIL);
        Customer customer = authIdentityService.getCustomerByIdentifier(identifier, verificationChannel);

        if (customer.isAccountVerified()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already verified");
        }

        verificationCodeService.generateAndSendCode(
                customer,
                verificationChannel,
                VerificationPurpose.REGISTER
        );

        return "Verification code sent";
    }

    @Override
    public String verifyRegister(String identifier, String channel, String code) {
        VerificationChannel verificationChannel = authIdentityService.parseChannel(channel, VerificationChannel.EMAIL);
        Customer customer = authIdentityService.getCustomerByIdentifier(identifier, verificationChannel);

        boolean valid = verificationCodeService.verifyCode(
                customer,
                code,
                verificationChannel,
                VerificationPurpose.REGISTER
        );

        if (!valid) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid code");
        }

        customer.setAccountVerified(true);
        customerRepository.save(customer);

        return "Account verified successfully";
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
