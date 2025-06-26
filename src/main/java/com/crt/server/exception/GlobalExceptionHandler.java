package com.crt.server.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.EntityNotFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
                log.error("Unhandled exception occurred: ", ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred")
                                .error("Internal Server Error")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();

                return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex,
                        HttpServletRequest request) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", HttpStatus.NOT_FOUND.value());
                body.put("error", "Not Found");
                body.put("message", ex.getMessage());
                body.put("path", request.getRequestURI());

                return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
        }

        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<Map<String, Object>> handleMaxSizeException(MaxUploadSizeExceededException ex,
                        HttpServletRequest request) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", HttpStatus.PAYLOAD_TOO_LARGE.value());
                body.put("error", "Payload Too Large");
                body.put("message", "File size exceeds maximum limit");
                body.put("path", request.getRequestURI());

                return new ResponseEntity<>(body, HttpStatus.PAYLOAD_TOO_LARGE);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException ex,
                        HttpServletRequest request) {
                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", HttpStatus.BAD_REQUEST.value());
                body.put("error", "Bad Request");
                body.put("message", ex.getMessage());
                body.put("path", request.getRequestURI());

                return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
        }

        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException ex,
                        WebRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.NOT_FOUND.value())
                                .message(ex.getMessage())
                                .error("Not Found")
                                .timestamp(LocalDateTime.now())
                                .path(request.getDescription(false))
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }

        // Handle validation errors
        @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationException(
                        org.springframework.web.bind.MethodArgumentNotValidException ex, HttpServletRequest request) {
                StringBuilder message = new StringBuilder("Validation failed: ");
                ex.getBindingResult().getFieldErrors().forEach(error -> 
                        message.append(error.getField()).append(" ").append(error.getDefaultMessage()).append("; ")
                );

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.BAD_REQUEST.value())
                                .message(message.toString())
                                .error("Validation Error")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }

        // Handle database constraint violations
        @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
        public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
                        org.springframework.dao.DataIntegrityViolationException ex, HttpServletRequest request) {
                String message = "Data integrity violation";
                if (ex.getMessage().contains("Duplicate entry")) {
                        message = "Duplicate entry - record already exists";
                } else if (ex.getMessage().contains("foreign key constraint")) {
                        message = "Cannot delete - record is referenced by other data";
                }

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.CONFLICT.value())
                                .message(message)
                                .error("Data Integrity Error")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
        }

        // Handle authentication errors
        @ExceptionHandler(org.springframework.security.authentication.BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(
                        org.springframework.security.authentication.BadCredentialsException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message("Invalid credentials")
                                .error("Authentication Error")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        // Handle access denied errors
        @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
        public ResponseEntity<ErrorResponse> handleAccessDenied(
                        org.springframework.security.access.AccessDeniedException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.FORBIDDEN.value())
                                .message("Access denied - insufficient permissions")
                                .error("Authorization Error")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
        }

        // Handle custom authentication exceptions
        @ExceptionHandler(AuthenticationException.class)
        public ResponseEntity<ErrorResponse> handleCustomAuthenticationException(
                        AuthenticationException ex, HttpServletRequest request) {
                ErrorResponse errorResponse = ErrorResponse.builder()
                                .status(HttpStatus.UNAUTHORIZED.value())
                                .message(ex.getMessage())
                                .error("Authentication Failed")
                                .timestamp(LocalDateTime.now())
                                .path(request.getRequestURI())
                                .build();
                return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }
}
