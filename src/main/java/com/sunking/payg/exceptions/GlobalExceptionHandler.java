package com.sunking.payg.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sunking.payg.dto.ApiErrorResponse;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {

        log.warn("Resource not found: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        404,
                        ex.getMessage(),
                        "NOT_FOUND",
                        System.currentTimeMillis()
                ),
                HttpStatus.NOT_FOUND
        );
    }


    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateResource(DuplicateResourceException ex) {

        log.warn("Duplicate resource: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        409,
                        ex.getMessage(),
                        "CONFLICT",
                        System.currentTimeMillis()
                ),
                HttpStatus.CONFLICT
        );
    }


    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusinessExceptions(BusinessException ex) {

        log.warn("Business exception: {}", ex.getMessage());

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        400,
                        ex.getMessage(),
                        "BAD_REQUEST",
                        System.currentTimeMillis()
                ),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");

        log.warn("Validation failed: {}", message);

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        400,
                        message,
                        "VALIDATION_ERROR",
                        System.currentTimeMillis()
                ),
                HttpStatus.BAD_REQUEST
        );
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(Exception ex) {

        log.error("Unhandled exception occurred", ex);

        return new ResponseEntity<>(
                new ApiErrorResponse(
                        500,
                        "Something went wrong",
                        "INTERNAL_SERVER_ERROR",
                        System.currentTimeMillis()
                ),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}