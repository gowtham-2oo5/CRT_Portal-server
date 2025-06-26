package com.crt.server.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    @Override
    public AuthResponseDTO login(AuthRequestDTO loginRequest) {
        User user = validateCredentials(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        if (userDTO == null) {
            throw new ResourceNotFoundException("User not found");
        }

        // Generate and store OTP
        String otp = otpService.generateOTP();
        otpService.storeOTP(loginRequest.getUsernameOrEmail(), otp);

        // Send OTP via email
        emailService.sendLoginOtp(otp, user.getEmail());

        String maskedEmail = maskEmail(user.getEmail());
        return AuthResponseDTO.builder()
                .message("OTP sent to " + maskedEmail)
                .user(userDTO)
                .build();
    }

    @Override
    public AuthResponseDTO verifyOTP(AuthRequestDTO otpVerification) {
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

        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        String token = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        System.out.println(user.isFirstLogin());
        System.out.println("HEREES THE TOKEN RA: " + token);

        return AuthResponseDTO.builder()
                .message("OTP verified successfully")
                .token(token)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    @Override
    public AuthResponseDTO refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        UserDTO userDTO = userService.getUserByEmail(user.getEmail());

        // Generate new tokens
        String newToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return AuthResponseDTO.builder()
                .message("Token refreshed successfully")
                .token(newToken)
                .refreshToken(newRefreshToken)
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
}