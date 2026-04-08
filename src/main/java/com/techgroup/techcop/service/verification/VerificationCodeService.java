package com.techgroup.techcop.service.verification;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.security.enums.VerificationChannel;
import com.techgroup.techcop.security.enums.VerificationPurpose;

public interface VerificationCodeService {

    void generateAndSendCode(Customer customer, VerificationChannel channel, VerificationPurpose purpose);

    boolean verifyCode(Customer customer,
                       String code,
                       VerificationChannel channel,
                       VerificationPurpose purpose);

}

