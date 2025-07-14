package com.crt.server.service.impl;

import com.crt.server.dto.AccountConfirmationMailDTO;
import com.crt.server.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Value("${client.url}")
    private String clientUrl;

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void sendPasswordEmail(String to, String username, String password) {
        sendEmail(to, "Your Account Credentials", buildPasswordEmailContent(username, password), "password email");
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        sendEmail(to, "Password Reset Request", buildPasswordResetEmailContent(resetToken), "password reset email");
    }

    @Override
    public void sendLoginOtp(String otp, String mail) {
        log.info("Login OTP: {}", otp);
        sendEmail(mail, "Your Login OTP Code", buildOtpEmailContent(otp), "OTP");
    }

    @Override
    public void sendStudentAccountConfirmationMail(String email, AccountConfirmationMailDTO student) {
        sendEmail(email, "Welcome to Course Registration Portal", buildStudentConfirmationEmailContent(student),
                "student confirmation email");
    }

    private void sendEmail(String to, String subject, String content, String emailType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
            log.info("{} sent to: {}", emailType, to);
        } catch (Exception e) {
            log.error("Failed to send {}: {}", emailType, e.getMessage());
            throw new RuntimeException("Failed to send " + emailType, e);
        }
    }

    private String buildPasswordEmailContent(String username, String password) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .header { background-color: #2a9df4; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .credentials { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <div class='header'>
                            <h2>Your Account Credentials</h2>
                        </div>
                        <div class='content'>
                            <p>Hello,</p>
                            <p>Your account has been created successfully. Here are your login credentials:</p>
                            <div class='credentials'>
                                <p><strong>Username:</strong> %s</p>
                                <p><strong>Password:</strong> %s</p>
                            </div>
                            <p>For security reasons, we recommend changing your password after your first login.</p>
                            <p>You can login at: <a href='%s'>%s</a></p>
                        </div>
                        <div class='footer'>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(username, password, clientUrl, clientUrl);
    }

    private String buildPasswordResetEmailContent(String resetToken) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; line-height: 1.6; }
                        .container { max-width: 600px; margin: 20px auto; padding: 20px; border: 1px solid #ddd; border-radius: 8px; }
                        .header { background-color: #2a9df4; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                        .content { padding: 20px; }
                        .reset-token { background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 20px 0; text-align: center; font-size: 18px; }
                        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                    </style>
                </head>
                <body>
                    <div class='container'>
                        <div class='header'>
                            <h2>Password Reset Request</h2>
                        </div>
                        <div class='content'>
                            <p>Hello,</p>
                            <p>We received a request to reset your password. Use the following token to reset your password:</p>
                            <div class='reset-token'>%s</div>
                            <p>If you didn't request this, please ignore this email.</p>
                            <p>You can reset your password at: <a href='%s/reset-password'>%s/reset-password</a></p>
                        </div>
                        <div class='footer'>
                            <p>This is an automated message, please do not reply.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(resetToken, clientUrl, clientUrl);
    }

    private String buildOtpEmailContent(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: Arial, sans-serif; color: #333; }
                        .otp-container { background-color: #f4f4f4; padding: 20px; text-align: center; border-radius: 8px; width: 300px; margin: 0 auto; border: 1px solid #ddd; }
                        .otp-code { font-size: 24px; font-weight: bold; color: #2a9df4; margin-top: 10px; }
                    </style>
                </head>
                <body>
                    <div class='otp-container'>
                        <p>Your OTP code is:</p>
                        <p class='otp-code'>%s</p>
                    </div>
                </body>
                </html>
                """
                .formatted(otp);
    }

    private String buildStudentConfirmationEmailContent(AccountConfirmationMailDTO student) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                    <style>
                        body { font-family: 'Arial', sans-serif; color: #333; line-height: 1.6; background-color: #f4f4f4; margin: 0; padding: 0; }
                        .email-container { max-width: 600px; margin: 20px auto; background-color: white; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); overflow: hidden; }
                        .header { background-color: #2a9df4; color: white; padding: 30px 0; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; text-transform: uppercase; letter-spacing: 2px; }
                        .content { padding: 40px 30px; text-align: center; }
                        .content h2 { color: #2a9df4; margin-bottom: 20px; }
                        .content p { font-size: 16px; margin-bottom: 30px; }
                        .credentials { background-color: #f9f9f9; padding: 20px; border-radius: 8px; margin-bottom: 30px; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
                        .credentials h3 { color: #2a9df4; margin-bottom: 15px; }
                        .cta-button { display: inline-block; background-color: #2a9df4; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; font-weight: bold; text-transform: uppercase; letter-spacing: 1px; transition: background-color 0.3s ease; }
                        .cta-button:hover { background-color: #1a7db7; }
                        .footer { background-color: #f4f4f4; padding: 20px; text-align: center; font-size: 14px; color: #666; }
                        .footer p { margin: 0; }
                    </style>
                </head>
                <body>
                    <div class='email-container'>
                        <div class='header'>
                            <h1>Course Registration Portal</h1>
                        </div>
                        <div class='content'>
                            <h2>Welcome, %s!</h2>
                            <p>Your account has been successfully created in the Course Registration Portal. Get ready for an exciting learning journey!</p>
                            <div class='credentials'>
                                <h3>Your Login Credentials</h3>
                                <p><strong>Username:</strong> %s</p>
                                <p><strong>Temporary Password:</strong> %s</p>
                                <p><em>For security reasons, we recommend changing your password after your first login.</em></p>
                            </div>
                            <div>
                                <h3>What's Next?</h3>
                                <ol style='text-align: left; padding-left: 20px;'>
                                    <li>Log in to your account</li>
                                    <li>Complete your profile</li>
                                    <li>Browse available courses</li>
                                    <li>Start your learning adventure!</li>
                                </ol>
                            </div>
                            <a href='%s' class='cta-button'>Go to Portal</a>
                        </div>
                        <div class='footer'>
                            <p>If you have any questions, please don't hesitate to contact our support team.</p>
                            <p>&copy; 2024 Course Registration Portal. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """
                .formatted(student.getName(), student.getUsername(), student.getPassword(), clientUrl);
    }
}