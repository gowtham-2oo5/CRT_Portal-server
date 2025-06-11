package com.crt.server.service;

import com.crt.server.dto.AccountConfirmationMailDTO;

public interface EmailService {
    void sendPasswordEmail(String to, String username, String password);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendLoginOtp(String otp, String mail);

    void sendStudentAccountConfirmationMail(String email, AccountConfirmationMailDTO student);
}