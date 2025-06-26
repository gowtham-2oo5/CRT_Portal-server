package com.crt.server.security;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class OTPService {
    // In-memory storage for OTPs (keyed by username or email)
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();

    public String generateOTP() {
        return String.format("%06d", (int) (Math.random() * 1_000_000));
    }

    public void storeOTP(String usernameOrEmail, String otp) {
        System.out.println("Storing OTP for " + usernameOrEmail);
        otpStorage.put(usernameOrEmail, otp);
    }

    public boolean verifyOTP(String usernameOrEmail, String otp) {
        System.out.println("Verifying OTP for " + usernameOrEmail);
        String storedOTP = otpStorage.get(usernameOrEmail);
        if (storedOTP == null) {
            return false;
        }

        boolean isValid = storedOTP.equals(otp);
        if (isValid) {
            otpStorage.remove(usernameOrEmail);
        }
        removeOTP(usernameOrEmail);
        return isValid;
    }

    public void removeOTP(String usernameOrEmail) {
        otpStorage.remove(usernameOrEmail);
    }
}
