package org.raoamigos.trackingservice.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.trackingservice.dto.DeliveryUpdateEvent;
import org.raoamigos.trackingservice.entity.HubLocation;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingEventListenerImplTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @InjectMocks
    private TrackingEventListenerImpl trackingEventListener;

    @Test
    void handleDeliveryUpdate_ShouldSaveNewTrackingEvent() {
        DeliveryUpdateEvent incomingEvent = new DeliveryUpdateEvent();
        incomingEvent.setTrackingNumber("TRK999");
        incomingEvent.setStatus("SHIPPED");
        incomingEvent.setMessage("Package left facility");

        trackingEventListener.handleDeliveryUpdate(incomingEvent);

        ArgumentCaptor<TrackingEvent> eventCaptor = ArgumentCaptor.forClass(TrackingEvent.class);
        verify(trackingEventRepository, times(1)).save(eventCaptor.capture());

        TrackingEvent savedEvent = eventCaptor.getValue();
        assertEquals("TRK999", savedEvent.getTrackingNumber());
        assertEquals("SHIPPED", savedEvent.getStatus());
        assertEquals(HubLocation.SYSTEM_GENERATED, savedEvent.getLocation());
    }
}