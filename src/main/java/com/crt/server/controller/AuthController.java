package com.crt.server.controller;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.exception.ErrorResponse;
import com.crt.server.security.RateLimitService;
import com.crt.server.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private com.crt.server.security.CookieService cookieService;
    @Autowired
    private RateLimitService rateLimitService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequestDTO loginRequest, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // Rate limit: 5 login attempts per minute per IP
        if (!rateLimitService.isAllowed("login:" + clientIp, 5, Duration.ofMinutes(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(429)
                    .error("Too Many Requests")
                    .message("Too many login attempts. Please try again later.")
                    .path("/api/auth/login")
                    .build());
        }
        
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@RequestBody AuthRequestDTO otpVerification, 
                                      HttpServletResponse response, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // Rate limit: 10 OTP attempts per minute per IP
        if (!rateLimitService.isAllowed("otp:" + clientIp, 10, Duration.ofMinutes(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(429)
                    .error("Too Many Requests")
                    .message("Too many OTP attempts. Please try again later.")
                    .path("/api/auth/verify-otp")
                    .build());
        }
        
        return ResponseEntity.ok(authService.verifyOTP(otpVerification, response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookies(request);
        if (refreshToken == null || refreshToken.isEmpty()) {
            ErrorResponse error = ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                    .message("Refresh token not found in cookies")
                    .path("/api/auth/refresh-token")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        return ResponseEntity.ok(authService.refreshToken(refreshToken, response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email, HttpServletRequest request) {
        String clientIp = getClientIp(request);
        
        // Rate limit: 3 password reset attempts per hour per IP
        if (!rateLimitService.isAllowed("reset:" + clientIp, 3, Duration.ofHours(1))) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErrorResponse.builder()
                    .timestamp(LocalDateTime.now())
                    .status(429)
                    .error("Too Many Requests")
                    .message("Too many password reset attempts. Please try again later.")
                    .path("/api/auth/forgot-password")
                    .build());
        }
        
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.noContent().build();
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
