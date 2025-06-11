package com.crt.server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OTPService {
    // In-memory storage for OTPs (in production, use Redis or similar)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOTP() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    public void storeOTP(String email, String otp) {
        otpStorage.put(email, otp);
    }

    public boolean verifyOTP(String email, String otp) {
        String storedOTP = otpStorage.get(email);
        if (storedOTP == null) {
            return false;
        }

        boolean isValid = storedOTP.equals(otp);
        if (isValid) {
            otpStorage.remove(email);
        }
        return isValid;
    }

    public void removeOTP(String email) {
        otpStorage.remove(email);
    }
}