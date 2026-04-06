package com.techgroup.techcop.security.password;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashingServiceTest {

    private final PasswordHashingService passwordHashingService =
            new PasswordHashingService(new BCryptPasswordEncoder());

    @Test
    void encodeIfNeededHashesPlainTextPasswords() {
        String encoded = passwordHashingService.encodeIfNeeded("plain-password");

        assertNotEquals("plain-password", encoded);
        assertTrue(passwordHashingService.isBcryptHash(encoded));
        assertTrue(passwordHashingService.matches("plain-password", encoded));
    }

    @Test
    void encodeIfNeededDoesNotRehashExistingBcryptPasswords() {
        String existingHash = passwordHashingService.hashNewPassword("plain-password");

        String sameHash = passwordHashingService.encodeIfNeeded(existingHash);

        assertEquals(existingHash, sameHash);
    }

    @Test
    void matchesSupportsLegacyPlainTextPasswords() {
        assertTrue(passwordHashingService.matches("legacy-password", "legacy-password"));
    }
}
