package org.raoamigos.notificationservice.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.notificationservice.dto.DeliveryBookedEvent;
import org.raoamigos.notificationservice.service.EmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryBookedListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeliveryBookedListener listener;

    @Test
    @DisplayName("Scenario 2: handleDeliveryBooked() delegates to EmailService")
    void handleDeliveryBooked_ShouldCallEmailService() {
        DeliveryBookedEvent event = new DeliveryBookedEvent("user@test.com", "User", "TRK1", "Rec", "City", 99.0, "PAY_NOW");

        listener.handleDeliveryBooked(event);

        verify(emailService, times(1)).sendDeliveryBookedEmail(event);
    }
}
