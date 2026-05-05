package org.raoamigos.notificationservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// import org.raoamigos.notificationservice.dto.DeliveryBookedEvent;
// import org.raoamigos.notificationservice.dto.DeliveryDeliveredEvent;
// import org.raoamigos.notificationservice.dto.PasswordResetEvent;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

// import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@smartcourier.com");
    }

    @Test
    @DisplayName("Scenario 6: sendDeliveryBookedEmail(null) logs warning and returns without throwing")
    void sendDeliveryBookedEmail_WhenEventIsNull_ShouldReturnSafely() {
        assertDoesNotThrow(() -> emailService.sendDeliveryBookedEmail(null));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("Scenario 7: sendDeliveryDeliveredEmail(null) logs warning and returns without throwing")
    void sendDeliveryDeliveredEmail_WhenEventIsNull_ShouldReturnSafely() {
        assertDoesNotThrow(() -> emailService.sendDeliveryDeliveredEmail(null));
        verify(mailSender, never()).createMimeMessage();
    }

    @Test
    @DisplayName("Scenario 8: sendPasswordResetEmail(null) logs warning and returns without throwing")
    void sendPasswordResetEmail_WhenEventIsNull_ShouldReturnSafely() {
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(null));
        verify(mailSender, never()).createMimeMessage();
    }
}
