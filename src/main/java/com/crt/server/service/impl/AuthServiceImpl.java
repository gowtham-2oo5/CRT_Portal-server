package com.crt.server.service.impl;

import com.crt.server.security.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import com.crt.server.dto.PasswordUpdateDTO;
import com.crt.server.dto.UserDTO;
import com.crt.server.exception.AuthenticationException;
import com.crt.server.exception.ResourceNotFoundException;
import com.crt.server.model.User;
import com.crt.server.repository.UserRepository;
import com.crt.server.security.JwtService;
import com.crt.server.security.OTPService;
import com.crt.server.security.PasswordService;
import com.crt.server.service.AuthService;
import com.crt.server.service.EmailService;
import com.crt.server.service.UserService;

import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordService passwordService;
    private final OTPService otpService;
    private final JwtService jwtService;
    private final CookieService cookieService;
    private final StringRedisTemplate stringRedisTemplate;

    static {
        bypassMails.add("admin@kluniversity.in");
    }

    @Override
    public AuthResponseDTO login(AuthRequestDTO loginRequest) {
        User user = validateCredentials(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        if (userDTO == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Generate OTP
        String otp;
        
        // Handle OTP bypass for specific emails
        if (bypassMails.contains(user.getEmail())) {
            // For bypass emails, use a fixed OTP for easy testing
            otp = "123456";
            otpService.storeOTP(loginRequest.getUsernameOrEmail(), otp);
            log.info("OTP bypass activated for email: {}, OTP: {}", user.getEmail(), otp);
        } else {
            otp = otpService.generateOTP();
            otpService.storeOTP(loginRequest.getUsernameOrEmail(), otp);
            emailService.sendLoginOtp(otp, user.getEmail());
        }

        String maskedEmail = maskEmail(user.getEmail());
        return AuthResponseDTO.builder()
                .message("OTP sent to " + maskedEmail)
                .user(userDTO)
                .build();
    }

    @Override
    public AuthResponseDTO verifyOTP(AuthRequestDTO otpVerification, HttpServletResponse response) {
        String input = otpVerification.getUsernameOrEmail();

        User user;
        if (userRepository.existsByEmail(input)) {
            user = userRepository.findByEmail(input)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + input));
        } else if (userRepository.existsByUsername(input)) {
            user = userRepository.findByUsername(input)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + input));
        } else {
            throw new ResourceNotFoundException("User not found with username or email: " + input);
        }

        if (!otpService.verifyOTP(otpVerification.getUsernameOrEmail(), otpVerification.getOtp())) {
            throw new AuthenticationException("Invalid OTP");
        }

        boolean wasFirst = user.isFirstLogin();
        log.info("Fetching first login: " + wasFirst + " user: " + user.getName());
        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        String token = jwtService.generateToken(user);
        if (wasFirst) userService.updateFirstLoginStatus(user.getEmail(), false);

        String refreshTokenString = jwtService.generateRefreshToken(user);
        stringRedisTemplate.opsForValue().set(refreshTokenString, user.getUsername(), jwtService.getRefreshExpiration(), TimeUnit.MILLISECONDS);

        if (wasFirst) userService.updateFirstLoginStatus(user.getEmail(), false);
        log.info("WAs FIRST: " + wasFirst);
        cookieService.createAccessTokenCookie(token, response);
        cookieService.createRefreshTokenCookie(refreshTokenString, response);

        userDTO.setIsFirstLogin(wasFirst);
        return AuthResponseDTO.builder()
                .message("OTP verified successfully")
                .user(userDTO)
                .isFirstLogin(wasFirst)
                .build();
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken, HttpServletResponse response) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if refresh token exists in Redis
        String storedUsername = stringRedisTemplate.opsForValue().get(refreshToken);
        if (storedUsername == null || !storedUsername.equals(username)) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        // Delete old refresh token from Redis
        stringRedisTemplate.delete(refreshToken);

        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        // Generate new tokens
        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // Store new refresh token in Redis
        stringRedisTemplate.opsForValue().set(newRefreshToken, user.getUsername(), jwtService.getRefreshExpiration(), TimeUnit.MILLISECONDS);

        // Set new tokens as HttpOnly cookies
        cookieService.createAccessTokenCookie(newToken, response);
        cookieService.createRefreshTokenCookie(newRefreshToken, response);

        return AuthResponseDTO.builder()
                .message("Token refreshed successfully")
                .user(userDTO)
                .build();
    }

    @Override
    public AuthResponseDTO forgotPassword(String email) {
        // Verify user exists
        UserDTO user = userService.getUserByEmail(email);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Generate temporary password
        String tempPassword = passwordService.generateTemporaryPassword();

        // Update user's password in database
        PasswordUpdateDTO passwordUpdate = PasswordUpdateDTO.builder()
                .email(email)
                .newPassword(tempPassword)
                .build();
        passwordService.updatePasswordByEmail(passwordUpdate);

        // Send temporary password via email
        emailService.sendPasswordResetEmail(email, tempPassword);

        String maskedEmail = maskEmail(email);
        return AuthResponseDTO.builder()
                .message("Temporary password has been sent to " + maskedEmail)
                .build();
    }

    private User validateCredentials(String usernameOrEmail, String password) {
        // First, try username validation as it's typically faster
        if (isValidUsername(usernameOrEmail)) {
            return validateUsernameAndPassword(usernameOrEmail, password);
        }

        // If not a username, try email validation
        return validateEmailAndPassword(usernameOrEmail, password);
    }

    private boolean isValidUsername(String input) {
        // Basic username validation - can be enhanced based on your requirements
        return input != null && !input.contains("@");
    }

    private User validateUsernameAndPassword(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("Invalid username or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid username or password");
        }

        return user;
    }

    private User validateEmailAndPassword(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthenticationException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        return user;
    }

    private String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "";
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String firstChar = email.substring(0, 1);
        String lastFourChars = email.substring(atIndex - 4, atIndex);
        return firstChar + "***" + lastFourChars + email.substring(atIndex);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = cookieService.getRefreshTokenFromCookies(request);

        if (refreshToken != null) {
            stringRedisTemplate.delete(refreshToken);
        }
        cookieService.clearAuthCookies(response);
    }
}
