package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.dto.RegisterCustomerRequest;

public interface RegistrationService {
    String registerRequest(RegisterCustomerRequest request);
    String resendRegisterCode(String identifier, String channel);
    String verifyRegister(String identifier, String channel, String code);
}
