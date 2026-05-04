package org.raoamigos.notificationservice.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.notificationservice.dto.OtpEmailEvent;
import org.raoamigos.notificationservice.service.EmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OtpEmailListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OtpEmailListener listener;

    @Test
    @DisplayName("Scenario 1: handleOtpEmail() delegates to EmailService")
    void handleOtpEmail_ShouldCallEmailService() {
        OtpEmailEvent event = new OtpEmailEvent("test@example.com", "123456");

        listener.handleOtpEmail(event);

        verify(emailService, times(1)).sendOtpEmail(event);
    }
}
