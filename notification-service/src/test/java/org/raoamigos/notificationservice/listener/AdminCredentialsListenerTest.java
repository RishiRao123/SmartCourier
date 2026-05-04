package org.raoamigos.notificationservice.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.notificationservice.dto.AdminCredentialsEvent;
import org.raoamigos.notificationservice.service.EmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminCredentialsListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AdminCredentialsListener listener;

    @Test
    @DisplayName("Scenario 5: handleAdminCredentials() delegates to EmailService")
    void handleAdminCredentials_ShouldCallEmailService() {
        AdminCredentialsEvent event = new AdminCredentialsEvent("admin@test.com", "admin1", "secret");

        listener.handleAdminCredentials(event);

        verify(emailService, times(1)).sendAdminCredentialsEmail(event);
    }
}
