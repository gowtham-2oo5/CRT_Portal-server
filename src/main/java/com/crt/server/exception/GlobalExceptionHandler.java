package com.crt.server.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(Exception.class)
        public ResponseEntity<Map<String, Object>> handleException(Exception ex, HttpServletRequest request) {
                log.error("Unhandled exception occurred: ", ex);

                Map<String, Object> body = new HashMap<>();
                body.put("timestamp", LocalDateTime.now());
                body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
                body.put("error", "Internal Server Error");
                body.put("message", "An unexpected error occurred");
                body.put("path", request.getRequestURI());

                return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
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
}