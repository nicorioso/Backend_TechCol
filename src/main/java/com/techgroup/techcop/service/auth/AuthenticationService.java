package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    String getGoogleClientId();

    boolean accountExists(String email);

    String login(String email, String password, String channel);

    AuthResponse verifyCode(String email, String code,
                            HttpServletResponse response);

    AuthResponse authenticateWithGoogle(String credential,
                                        HttpServletResponse response);

    AuthResponse refresh(HttpServletRequest request);

    void logout(HttpServletResponse response);
}
