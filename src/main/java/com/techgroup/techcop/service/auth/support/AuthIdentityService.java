package com.techgroup.techcop.service.auth.support;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.regex.Pattern;

@Service
public class AuthIdentityService {

    private static final String DEFAULT_COUNTRY_CODE = "+57";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern E164_PATTERN = Pattern.compile("^\\+[1-9]\\d{7,14}$");

    private final CustomerRepository customerRepository;

    public AuthIdentityService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public VerificationChannel parseChannel(String channel, VerificationChannel defaultChannel) {
        if (channel == null || channel.trim().isEmpty()) {
            return defaultChannel;
        }

        try {
            return VerificationChannel.valueOf(channel.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid verification channel", ex);
        }
    }

    public String normalizeIdentifier(String identifier, VerificationChannel channel) {
        String value = identifier == null ? "" : identifier.trim();

        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Identifier is required");
        }

        return switch (channel) {
            case EMAIL -> normalizeEmail(value);
            case SMS -> normalizePhone(value);
        };
    }

    public String normalizeEmail(String email) {
        String normalized = email == null ? "" : email.trim().toLowerCase();

        if (normalized.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (!EMAIL_PATTERN.matcher(normalized).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }

        return normalized;
    }

    public String normalizePhone(String phone) {
        String value = phone == null ? "" : phone.trim();

        if (value.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }

        boolean hasPlusPrefix = value.startsWith("+");
        String digits = value.replaceAll("\\D", "");

        if (digits.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El telefono debe estar en formato internacional E.164, ejemplo +573001234567."
            );
        }

        String candidate;
        if (hasPlusPrefix) {
            candidate = "+" + digits;
        } else if (digits.length() == 10) {
            candidate = DEFAULT_COUNTRY_CODE + digits;
        } else if (digits.length() >= 11 && digits.length() <= 15) {
            candidate = "+" + digits;
        } else {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El telefono debe estar en formato internacional E.164, ejemplo +573001234567."
            );
        }

        if (!E164_PATTERN.matcher(candidate).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El telefono debe estar en formato internacional E.164, ejemplo +573001234567."
            );
        }

        return candidate;
    }

    public Customer getCustomerByIdentifier(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.findByCustomerEmail(normalizedIdentifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            case SMS -> customerRepository.findByCustomerPhoneNumber(normalizedIdentifier)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        };
    }

    public boolean accountExists(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.existsByCustomerEmail(normalizedIdentifier);
            case SMS -> customerRepository.existsByCustomerPhoneNumber(normalizedIdentifier);
        };
    }

    public String resolveAuthenticationEmail(String identifier, VerificationChannel channel) {
        if (channel == VerificationChannel.EMAIL) {
            return normalizeEmail(identifier);
        }

        return getCustomerByIdentifier(identifier, channel).getCustomerEmail();
    }
}
