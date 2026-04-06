package com.techgroup.techcop.exception;

import java.time.Instant;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String message,
        List<ApiErrorDetail> errors,
        Instant timestamp,
        String path
) {
}
