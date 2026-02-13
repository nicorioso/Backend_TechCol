package com.techgroup.techcop.service.auth;

public interface AuthenticationService {
    String login(String email, String password);
    String verifyCode(String email, String code);
}