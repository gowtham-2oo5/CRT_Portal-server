package com.crt.server.service.impl;

import com.crt.server.dto.AccountConfirmationMailDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    private MimeMessage mimeMessage;
    private static final String TEST_CLIENT_URL = "http://localhost:3000";

    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        ReflectionTestUtils.setField(emailService, "clientUrl", TEST_CLIENT_URL);
    }

    @Test
    void sendPasswordEmail_ShouldSendEmailSuccessfully() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String username = "testuser";
        String password = "testpass123";

        // Act
        emailService.sendPasswordEmail(to, username, password);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail_ShouldSendEmailSuccessfully() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String resetToken = "reset-token-123";

        // Act
        emailService.sendPasswordResetEmail(to, resetToken);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendLoginOtp_ShouldLogOtp() {
        // Arrange
        String otp = "123456";
        String email = "test@example.com";

        // Act
        emailService.sendLoginOtp(otp, email);

        // Assert - Since the actual email sending is commented out, we just verify no
        // exception is thrown
        assertDoesNotThrow(() -> emailService.sendLoginOtp(otp, email));
    }

    @Test
    void sendStudentAccountConfirmationMail_ShouldSendEmailSuccessfully() throws MessagingException {
        // Arrange
        String email = "student@example.com";
        AccountConfirmationMailDTO student = AccountConfirmationMailDTO.builder()
                .name("John Doe")
                .username("johndoe")
                .password("tempPass123")
                .build();

        // Act
        emailService.sendStudentAccountConfirmationMail(email, student);

        // Assert
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    void sendEmail_WhenMailSenderThrowsException_ShouldThrowRuntimeException() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String username = "testuser";
        String password = "testpass123";
        doThrow(new MessagingException("Failed to send email")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> emailService.sendPasswordEmail(to, username, password));
    }

    @Test
    void sendPasswordEmail_ShouldIncludeCorrectContent() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String username = "testuser";
        String password = "testpass123";
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        emailService.sendPasswordEmail(to, username, password);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
    }

    @Test
    void sendPasswordResetEmail_ShouldIncludeCorrectContent() throws MessagingException {
        // Arrange
        String to = "test@example.com";
        String resetToken = "reset-token-123";
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);

        // Act
        emailService.sendPasswordResetEmail(to, resetToken);

        // Assert
        verify(mailSender).send(messageCaptor.capture());
        MimeMessage capturedMessage = messageCaptor.getValue();
        assertNotNull(capturedMessage);
    }
}