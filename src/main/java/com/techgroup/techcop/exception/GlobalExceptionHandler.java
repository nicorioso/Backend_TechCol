package com.techgroup.techcop.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                        HttpServletRequest request) {
        return buildValidationResponse(ex.getBindingResult(), request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiErrorResponse> handleBindException(BindException ex,
                                                                HttpServletRequest request) {
        return buildValidationResponse(ex.getBindingResult(), request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleHandlerMethodValidation(HandlerMethodValidationException ex,
                                                                          HttpServletRequest request) {
        List<ApiErrorDetail> errors = new ArrayList<>();

        for (ParameterValidationResult validationResult : ex.getParameterValidationResults()) {
            String parameterName = validationResult.getMethodParameter().getParameterName();

            if (validationResult instanceof ParameterErrors parameterErrors) {
                errors.addAll(extractBindingErrors(parameterErrors));
                continue;
            }

            for (MessageSourceResolvable resolvable : validationResult.getResolvableErrors()) {
                errors.add(new ApiErrorDetail(
                        parameterName,
                        defaultMessage(resolvable.getDefaultMessage(), "Valor invalido")
                ));
            }
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validacion", errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(ConstraintViolationException ex,
                                                                      HttpServletRequest request) {
        List<ApiErrorDetail> errors = ex.getConstraintViolations().stream()
                .map(violation -> new ApiErrorDetail(
                        extractFieldName(violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : null),
                        violation.getMessage()
                ))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "Error de validacion", errors, request);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MissingServletRequestParameterException.class,
            MissingServletRequestPartException.class,
            MethodArgumentTypeMismatchException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiErrorResponse> handleBadRequest(Exception ex,
                                                             HttpServletRequest request) {
        List<ApiErrorDetail> errors = List.of(new ApiErrorDetail("request", resolveBadRequestMessage(ex)));
        return buildResponse(HttpStatus.BAD_REQUEST, "Solicitud invalida", errors, request);
    }

    @ExceptionHandler(ProductInUseException.class)
    public ResponseEntity<ApiErrorResponse> handleProductInUse(ProductInUseException ex,
                                                               HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(AccessDeniedException ex,
                                                               HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage(), List.of(), request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex,
                                                                 HttpServletRequest request) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = defaultMessage(ex.getReason(), status.getReasonPhrase());
        return buildResponse(status, message, List.of(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralError(Exception ex,
                                                               HttpServletRequest request) {
        log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error interno en el servidor",
                List.of(),
                request
        );
    }

    private ResponseEntity<ApiErrorResponse> buildValidationResponse(BindingResult bindingResult,
                                                                     HttpServletRequest request) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Error de validacion",
                extractBindingErrors(bindingResult),
                request
        );
    }

    private List<ApiErrorDetail> extractBindingErrors(Errors errorsSource) {
        List<ApiErrorDetail> errors = new ArrayList<>();

        for (FieldError fieldError : errorsSource.getFieldErrors()) {
            errors.add(new ApiErrorDetail(
                    fieldError.getField(),
                    defaultMessage(fieldError.getDefaultMessage(), "Valor invalido")
            ));
        }

        for (ObjectError globalError : errorsSource.getGlobalErrors()) {
            errors.add(new ApiErrorDetail(
                    globalError.getObjectName(),
                    defaultMessage(globalError.getDefaultMessage(), "Solicitud invalida")
            ));
        }

        return errors;
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status,
                                                           String message,
                                                           List<ApiErrorDetail> errors,
                                                           HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(),
                message,
                errors,
                Instant.now(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }

    private String extractFieldName(String propertyPath) {
        if (propertyPath == null || propertyPath.isBlank()) {
            return "request";
        }

        String normalized = propertyPath;
        int lastDot = normalized.lastIndexOf('.');
        if (lastDot >= 0 && lastDot < normalized.length() - 1) {
            normalized = normalized.substring(lastDot + 1);
        }

        return normalized.replaceAll("\\[[^\\]]+\\]", "");
    }

    private String resolveBadRequestMessage(Exception ex) {
        if (ex instanceof MissingServletRequestParameterException missingParameterException) {
            return "Falta el parametro requerido: " + missingParameterException.getParameterName();
        }

        if (ex instanceof MissingServletRequestPartException missingPartException) {
            return "Falta la parte requerida: " + missingPartException.getRequestPartName();
        }

        if (ex instanceof MethodArgumentTypeMismatchException mismatchException) {
            return "El parametro '" + mismatchException.getName() + "' tiene un formato invalido";
        }

        if (ex instanceof HttpMessageNotReadableException) {
            return "El cuerpo de la solicitud no es valido";
        }

        return defaultMessage(ex.getMessage(), "Solicitud invalida");
    }

    private String defaultMessage(String message, String fallback) {
        return message == null || message.isBlank() ? fallback : message;
    }
}
