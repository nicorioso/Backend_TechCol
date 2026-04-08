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
import java.util.List;
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
        List<VerificationCode> activeCodes = repository.findByCustomerAndPurposeAndChannelAndUsedFalse(
                customer,
                purpose,
                channel
        );

        activeCodes.forEach(activeCode -> activeCode.setUsed(true));
        if (!activeCodes.isEmpty()) {
            repository.saveAll(activeCodes);
        }

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        VerificationCode verification = new VerificationCode();
        verification.setCode(code);
        verification.setExpirationTime(LocalDateTime.now().plusMinutes(5));
        verification.setUsed(false);
        verification.setAttempts(0);
        verification.setChannel(channel);
        verification.setPurpose(purpose);
        verification.setCustomer(customer);

        repository.save(verification);

        switch (channel) {
            case EMAIL -> emailService.sendVerificationCode(
                    customer.getCustomerEmail(),
                    code,
                    purpose
            );
            case SMS -> {
                if (customer.getCustomerPhoneNumber() == null || customer.getCustomerPhoneNumber().trim().isEmpty()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El usuario no tiene un telefono registrado");
                }
                smsService.sendSms(customer.getCustomerPhoneNumber(), buildSmsMessage(purpose, code));
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid channel");
        }
    }

    @Override
    public boolean verifyCode(Customer customer,
                              String code,
                              VerificationChannel channel,
                              VerificationPurpose purpose) {
        VerificationCode verification = repository
                .findTopByCustomerAndPurposeAndChannelAndUsedFalseOrderByExpirationTimeDesc(
                        customer,
                        purpose,
                        channel
                )
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

    private String buildSmsMessage(VerificationPurpose purpose, String code) {
        return switch (purpose) {
            case LOGIN -> "Tu codigo de inicio de sesion es: " + code;
            case REGISTER -> "Tu codigo de verificacion de cuenta es: " + code;
            case CHANGE_PASSWORD -> "Tu codigo para cambio de contrasena es: " + code;
        };
    }
}
