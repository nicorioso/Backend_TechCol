package com.techgroup.techcop.service.verification.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.VerificationCode;
import com.techgroup.techcop.repository.VerificationCodeRepository;
import com.techgroup.techcop.service.email.EmailService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeRepository repository;
    private final EmailService emailService;

    public VerificationCodeServiceImpl(VerificationCodeRepository repository,
                                       EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    @Override
    public void generateAndSendCode(Customer customer) {

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        VerificationCode verification = new VerificationCode();
        verification.setCode(code);
        verification.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        verification.setUsed(false);
        verification.setAttempts(0);
        verification.setCustomer(customer);

        repository.save(verification);

        emailService.sendVerificationCode(customer.getCustomerEmail(), code);

    }

    @Override
    public boolean verifyCode(Customer customer, String code) {

        VerificationCode verification = repository
                .findTopByCustomerAndUsedFalseOrderByExpirationTimeDesc(customer)
                .orElseThrow(() -> new RuntimeException("No active code"));

        if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Code expired");
        }

        if (!verification.getCode().equals(code)) {
            verification.setAttempts(verification.getAttempts() + 1);
            repository.save(verification);
            return false;
        }

        verification.setUsed(true);
        repository.save(verification);

        return true;
    }
}

