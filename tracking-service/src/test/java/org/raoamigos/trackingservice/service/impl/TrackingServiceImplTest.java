package org.raoamigos.trackingservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceImplTest {

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @InjectMocks
    private TrackingServiceImpl trackingService;

    private TrackingEvent dummyEvent;
    private final String TRACKING_NUMBER = "TRK12345678";

    @BeforeEach
    void setUp() {
        dummyEvent = TrackingEvent.builder()
                .trackingNumber(TRACKING_NUMBER)
                .status("IN_TRANSIT")
                .build();
    }

    @Test
    void getTrackingHistory_ShouldReturnList_WhenEventsExist() {
        when(trackingEventRepository.findByTrackingNumberOrderByTimestampDesc(TRACKING_NUMBER))
                .thenReturn(List.of(dummyEvent));

        List<TrackingEvent> result = trackingService.getTrackingHistory(TRACKING_NUMBER);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        verify(trackingEventRepository, times(1))
                .findByTrackingNumberOrderByTimestampDesc(TRACKING_NUMBER);
    }

    @Test
    void getTrackingHistory_ShouldThrowException_WhenNoEventsFound() {
        when(trackingEventRepository.findByTrackingNumberOrderByTimestampDesc(TRACKING_NUMBER))
                .thenReturn(Collections.emptyList());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                trackingService.getTrackingHistory(TRACKING_NUMBER)
        );

        assertEquals("No tracking history found for tracking number: " + TRACKING_NUMBER, exception.getMessage());
    }

    @Test
    void saveTrackingEvent_ShouldCallRepositorySave() {
        trackingService.saveTrackingEvent(dummyEvent);
        verify(trackingEventRepository, times(1)).save(dummyEvent);
    }
}