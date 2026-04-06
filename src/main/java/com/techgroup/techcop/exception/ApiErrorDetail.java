package com.techgroup.techcop.exception;

public record ApiErrorDetail(
        String field,
        String message
) {
}
