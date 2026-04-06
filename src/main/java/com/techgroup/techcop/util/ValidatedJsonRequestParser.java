package com.techgroup.techcop.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Component
public class ValidatedJsonRequestParser {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ValidatedJsonRequestParser(ObjectMapper objectMapper,
                                      Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public <T> T parse(String json, Class<T> targetType, String invalidJsonMessage) {
        if (json == null || json.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidJsonMessage);
        }

        try {
            T request = objectMapper.readValue(json, targetType);
            if (request == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidJsonMessage);
            }
            validate(request);
            return request;
        } catch (ConstraintViolationException ex) {
            throw ex;
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidJsonMessage, ex);
        }
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }
}
