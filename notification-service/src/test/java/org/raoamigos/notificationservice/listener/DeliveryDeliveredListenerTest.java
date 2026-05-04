package org.raoamigos.notificationservice.listener;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.notificationservice.dto.DeliveryDeliveredEvent;
import org.raoamigos.notificationservice.service.EmailService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeliveryDeliveredListenerTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private DeliveryDeliveredListener listener;

    @Test
    @DisplayName("Scenario 3: handleDeliveryDelivered() delegates to EmailService")
    void handleDeliveryDelivered_ShouldCallEmailService() {
        DeliveryDeliveredEvent event = new DeliveryDeliveredEvent("user@test.com", "TRK1", "Rec", "Delivered at door");

        listener.handleDeliveryDelivered(event);

        verify(emailService, times(1)).sendDeliveryDeliveredEmail(event);
    }
}
