package com.crt.server.service;

import com.crt.server.dto.AccountConfirmationMailDTO;

import java.util.List;

public interface EmailService {
    void sendPasswordEmail(String to, String username, String password);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendLoginOtp(String otp, String mail);

    void sendStudentAccountConfirmationMail(String email, AccountConfirmationMailDTO student);

    int sendBulkEmail(String subject, String body, List<String> emailIds);
}
