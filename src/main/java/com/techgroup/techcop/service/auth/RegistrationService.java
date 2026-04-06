package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.dto.RegisterCustomerRequest;

public interface RegistrationService {
    String registerRequest(RegisterCustomerRequest request);
    String verifyRegister(String email, String code);
}
