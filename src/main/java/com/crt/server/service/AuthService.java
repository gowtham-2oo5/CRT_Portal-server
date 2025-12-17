package com.crt.server.service;

import com.crt.server.dto.AuthRequestDTO;
import com.crt.server.dto.AuthResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashSet;
import java.util.Set;

public interface AuthService {

    Set<String> bypassMails = new HashSet<>();

    AuthResponseDTO login(AuthRequestDTO loginRequest);

    AuthResponseDTO verifyOTP(AuthRequestDTO otpVerification, HttpServletResponse response);

    AuthResponseDTO forgotPassword(String email);

    AuthResponseDTO refreshToken(String refreshToken, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);
}