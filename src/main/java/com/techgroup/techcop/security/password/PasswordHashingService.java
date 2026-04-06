package com.techgroup.techcop.security.password;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordHashingService {

    private final PasswordEncoder passwordEncoder;

    public PasswordHashingService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String hashNewPassword(String rawPassword) {
        if (!hasText(rawPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        return passwordEncoder.encode(rawPassword);
    }

    public String encodeIfNeeded(String passwordValue) {
        if (!hasText(passwordValue)) {
            return passwordValue;
        }

        return isBcryptHash(passwordValue) ? passwordValue : passwordEncoder.encode(passwordValue);
    }

    public boolean matches(String rawPassword, String storedPassword) {
        if (!hasText(rawPassword) || !hasText(storedPassword)) {
            return false;
        }

        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        return rawPassword.equals(storedPassword);
    }

    public boolean isBcryptHash(String passwordValue) {
        return hasText(passwordValue)
                && (passwordValue.startsWith("$2a$")
                || passwordValue.startsWith("$2b$")
                || passwordValue.startsWith("$2y$"));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
