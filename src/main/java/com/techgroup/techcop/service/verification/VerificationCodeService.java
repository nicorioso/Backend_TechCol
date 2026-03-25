package com.techgroup.techcop.service.verification;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.security.enums.VerificationChannel;

public interface VerificationCodeService {

    void generateAndSendCode(Customer customer, VerificationChannel channel);

    boolean verifyCode(Customer customer, String code);

}

