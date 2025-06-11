package com.crt.server.service;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;

public interface AuthService {
    AuthResponseDTO login(AuthRequestDTO loginRequest);

    AuthResponseDTO verifyOTP(AuthRequestDTO otpVerification);

    AuthResponseDTO forgotPassword(String email);

    AuthResponseDTO refreshToken(String refreshToken);
}