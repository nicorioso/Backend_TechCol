package com.techgroup.techcop.service.email;

import com.techgroup.techcop.security.enums.VerificationPurpose;

public interface EmailService {

    void sendVerificationCode(String to, String code, VerificationPurpose purpose);

}

