package com.crm.common.exception;

import com.crm.common.response.StandardResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<Object>> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<StandardResponse<Object>> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<StandardResponse<Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message("Invalid email or password")
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardResponse<Object>> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message("Access denied: You do not have permission to access this resource")
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ErrorResponse.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = (error instanceof FieldError) ? ((FieldError) error).getField() : error.getObjectName();
                    return new ErrorResponse.FieldErrorDetail(fieldName, error.getDefaultMessage());
                })
                .toList();

        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message("Validation failed for input arguments")
                .data(fieldErrors)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<StandardResponse<Object>> handleConstraintViolation(jakarta.validation.ConstraintViolationException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.web.method.annotation.HandlerMethodValidationException.class)
    public ResponseEntity<StandardResponse<Object>> handleMethodValidation(org.springframework.web.method.annotation.HandlerMethodValidationException ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message("Validation failed: " + ex.getMessage())
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<Object>> handleGenericException(Exception ex, HttpServletRequest request) {
        StandardResponse<Object> errorResponse = StandardResponse.builder()
                .success(false)
                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred")
                .data(null)
                .timestamp(Instant.now())
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
