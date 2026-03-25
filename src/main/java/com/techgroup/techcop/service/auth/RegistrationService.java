package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.entity.Customer;

public interface RegistrationService {
    String registerRequest(Customer customer);
    String verifyRegister(String email, String code);
}
