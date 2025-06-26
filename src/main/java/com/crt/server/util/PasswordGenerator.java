package com.crt.server.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordGenerator {
    private static final SecureRandom random = new SecureRandom();
    private static final int PASSWORD_LENGTH = 12;

    public static String generatePassword() {
        byte[] randomBytes = new byte[PASSWORD_LENGTH];
        random.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}