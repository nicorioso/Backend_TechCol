package com.techgroup.techcop.service.auth.support;

import com.techgroup.techcop.model.entity.Customer;
import com.techgroup.techcop.repository.CustomerRepository;
import com.techgroup.techcop.security.enums.VerificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class AuthIdentityService {

    private static final Logger log = LoggerFactory.getLogger(AuthIdentityService.class);
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

    public Optional<Customer> findCustomerByIdentifier(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.findByCustomerEmail(normalizedIdentifier);
            case SMS -> {
                List<Customer> customers = findCustomersByPhoneCandidates(normalizedIdentifier);
                yield resolveSingleSmsCustomer(normalizedIdentifier, customers);
            }
        };
    }

    public Customer getCustomerByIdentifier(String identifier, VerificationChannel channel) {
        return findCustomerByIdentifier(identifier, channel)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    public boolean accountExists(String identifier, VerificationChannel channel) {
        String normalizedIdentifier = normalizeIdentifier(identifier, channel);

        return switch (channel) {
            case EMAIL -> customerRepository.findByCustomerEmail(normalizedIdentifier).isPresent();
            case SMS -> !findCustomersByPhoneCandidates(normalizedIdentifier).isEmpty();
        };
    }

    public String resolveAuthenticationEmail(String identifier, VerificationChannel channel) {
        if (channel == VerificationChannel.EMAIL) {
            return normalizeEmail(identifier);
        }

        return getCustomerByIdentifier(identifier, channel).getCustomerEmail();
    }

    private List<Customer> findCustomersByPhoneCandidates(String normalizedPhone) {
        List<Customer> matches = new ArrayList<>();
        Set<String> seenCustomers = new HashSet<>();

        for (String candidate : buildPhoneLookupCandidates(normalizedPhone)) {
            List<Customer> customers = customerRepository.findAllByCustomerPhoneNumberOrderByCustomerIdAsc(candidate);
            for (Customer customer : customers) {
                Integer customerId = customer.getCustomerId();
                String uniqueKey = customerId == null ? customer.getCustomerEmail() : String.valueOf(customerId);
                if (seenCustomers.add(uniqueKey)) {
                    matches.add(customer);
                }
            }
        }

        matches.sort(Comparator.comparing(
                customer -> customer.getCustomerId() == null ? Integer.MAX_VALUE : customer.getCustomerId()
        ));
        return matches;
    }

    private Optional<Customer> resolveSingleSmsCustomer(String normalizedPhone, List<Customer> customers) {
        if (customers.isEmpty()) {
            return Optional.empty();
        }

        if (customers.size() > 1) {
            log.warn("Se encontraron multiples cuentas para el telefono {}. total={}", normalizedPhone, customers.size());
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El numero de telefono esta asociado a varias cuentas. Inicia sesion con correo o contacta soporte."
            );
        }

        return Optional.of(customers.getFirst());
    }

    private Set<String> buildPhoneLookupCandidates(String normalizedPhone) {
        Set<String> candidates = new LinkedHashSet<>();
        candidates.add(normalizedPhone);

        String digitsOnly = normalizedPhone.replaceAll("\\D", "");
        if (!digitsOnly.isEmpty()) {
            candidates.add(digitsOnly);
        }

        String countryDigits = DEFAULT_COUNTRY_CODE.replaceAll("\\D", "");
        if (!countryDigits.isEmpty()
                && digitsOnly.startsWith(countryDigits)
                && digitsOnly.length() > countryDigits.length()) {
            candidates.add(digitsOnly.substring(countryDigits.length()));
        }

        return candidates;
    }
}
