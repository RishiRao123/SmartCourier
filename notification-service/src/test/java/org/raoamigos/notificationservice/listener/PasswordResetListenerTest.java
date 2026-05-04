package org.raoamigos.notificationservice.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.notificationservice.dto.PasswordResetEvent;
import org.raoamigos.notificationservice.service.EmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordResetListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private PasswordResetListener listener;

    @Test
    @DisplayName("Scenario 4: handlePasswordReset() delegates to EmailService")
    void handlePasswordReset_ShouldCallEmailService() {
        PasswordResetEvent event = new PasswordResetEvent("user@test.com", "123456");

        listener.handlePasswordReset(event);

        verify(emailService, times(1)).sendPasswordResetEmail(event);
    }
}
