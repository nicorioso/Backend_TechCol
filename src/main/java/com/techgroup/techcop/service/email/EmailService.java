package com.techgroup.techcop.service.email;

public interface EmailService {

    void sendVerificationCode(String to, String code);

}

