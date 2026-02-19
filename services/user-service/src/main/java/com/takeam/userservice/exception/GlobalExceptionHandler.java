package com.takeam.userservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Standard error response builder
    private Map<String, Object> buildErrorResponse(
            HttpStatus status,
            String message,
            String path
    ) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return errorResponse;
    }

    // Authentication errors (401)
    @ExceptionHandler({BadCredentialsException.class, UsernameNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Authentication error: {}", ex.getMessage());

        String message = "Invalid email or password.";

        if (ex.getMessage() != null) {
            String lower = ex.getMessage().toLowerCase();
            if (lower.contains("locked")) {
                message = "Your account is locked. Please contact support.";
            } else if (lower.contains("disabled")) {
                message = "Your account is disabled. Please contact support.";
            }
        }

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // Validation errors (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex,
            WebRequest request
    ) {
        log.error("Validation error: {}", ex.getMessage());

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid input. Please check the fields and try again.",
                request.getDescription(false).replace("uri=", "")
        );

        error.put("fieldErrors", fieldErrors);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Illegal Argument (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            WebRequest request
    ) {
        log.error("Illegal argument: {}", ex.getMessage());

        String message = "Invalid request. Please check your input.";

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Illegal State (400)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalStateException(
            IllegalStateException ex,
            WebRequest request
    ) {
        log.error("Illegal state: {}", ex.getMessage());

        String message = "The requested operation cannot be completed.";

        if (ex.getMessage() != null) {
            String lower = ex.getMessage().toLowerCase();
            if (lower.contains("already exists")) {
                message = "This resource already exists.";
            } else if (lower.contains("not found")) {
                message = "The requested resource was not found.";
            }
        }

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    // Runtime Exception (500)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex,
            WebRequest request
    ) {
        log.error("Runtime error: ", ex);

        String message = "An error occurred. Please try again later.";

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Generic Exception (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex,
            WebRequest request
    ) {
        log.error("Unexpected error: ", ex);

        String message = "An unexpected error occurred. Please try again.";

        Map<String, Object> error = buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

//    // Custom application exception
//    @ExceptionHandler(ApplicationException.class)
//    public ResponseEntity<Map<String, Object>> handleApplicationException(
//            ApplicationException ex,
//            WebRequest request
//    ) {
//        log.error("Application error: {}", ex.getMessage());
//
//        Map<String, Object> error = buildErrorResponse(
//                ex.getStatus(),
//                ex.getMessage(),
//                request.getDescription(false).replace("uri=", "")
//        );
//
//        return new ResponseEntity<>(error, ex.getStatus());
//    }
}
