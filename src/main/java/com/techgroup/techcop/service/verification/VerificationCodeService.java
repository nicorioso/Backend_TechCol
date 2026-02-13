package com.techgroup.techcop.service.verification;

import com.techgroup.techcop.model.entity.Customer;

public interface VerificationCodeService {

    void generateAndSendCode(Customer customer);

    boolean verifyCode(Customer customer, String code);

}

