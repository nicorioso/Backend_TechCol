package com.techgroup.techcop.service.auth;

import com.techgroup.techcop.model.dto.AuthResponse;
import com.techgroup.techcop.model.dto.GoogleAuthRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthenticationService {

    String login(String email, String password);

    AuthResponse loginWithGoogle(
            GoogleAuthRequest request,
            HttpServletResponse response
    ) throws Exception;

    AuthResponse verifyCode(String email, String code,
                            HttpServletResponse response);

    AuthResponse refresh(HttpServletRequest request);

    void logout(HttpServletResponse response);
}