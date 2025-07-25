package com.crt.server.service;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO loginRequest);

    AuthResponseDTO verifyOTP(AuthRequestDTO otpVerification, HttpServletResponse response);

    AuthResponseDTO forgotPassword(String email);

    AuthResponseDTO refreshToken(String refreshToken, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}