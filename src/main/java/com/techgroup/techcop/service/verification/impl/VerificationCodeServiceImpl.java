package com.techgroup.techcop.service.verification.impl;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.model.entity.VerificationCode;
import com.techgroup.techcop.repository.VerificationCodeRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;
import com.techgroup.techcop.service.email.EmailService;
import com.techgroup.techcop.service.sms.SmsService;
import com.techgroup.techcop.service.verification.VerificationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    private final VerificationCodeRepository repository;
    private final EmailService emailService;
    private final SmsService smsService;

    public VerificationCodeServiceImpl(VerificationCodeRepository repository,
                                       EmailService emailService,
                                       SmsService smsService) {
        this.repository = repository;
        this.emailService = emailService;
        this.smsService = smsService;
    }

    @Override
    public void generateAndSendCode(Customer customer, VerificationChannel channel, VerificationPurpose purpose) {

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        VerificationCode verification = new VerificationCode();
        verification.setCode(code);
        verification.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        verification.setUsed(false);
        verification.setAttempts(0);
        verification.setCustomer(customer);

        repository.save(verification);

        switch (channel) {

            case EMAIL:
                emailService.sendVerificationCode(
                        customer.getCustomerEmail(),
                        code,
                        purpose
                );
                break;

            case SMS:
                smsService.sendSms(
                        customer.getCustomerPhoneNumber(),
                        "Tu código es: " + code
                );
                break;

            default:
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid channel");
        }
    }

    @Override
    public boolean verifyCode(Customer customer, String code) {

        VerificationCode verification = repository
                .findTopByCustomerAndUsedFalseOrderByExpirationTimeDesc(customer)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active code"));

        if (verification.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code expired");
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

