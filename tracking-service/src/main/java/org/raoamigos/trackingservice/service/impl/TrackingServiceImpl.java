package org.raoamigos.trackingservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.raoamigos.trackingservice.service.TrackingService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrackingServiceImpl implements TrackingService {

    private final TrackingEventRepository trackingEventRepository;

    @Override
    public List<TrackingEvent> getTrackingHistory(String trackingNumber) {
        List<TrackingEvent> events = trackingEventRepository.findByTrackingNumberOrderByTimestampDesc(trackingNumber);

        if (events.isEmpty()) {
            throw new RuntimeException("No tracking history found for tracking number: " + trackingNumber);
        }

        return events;
    }

    @Override
    public List<TrackingEvent> getAllTrackings() {
        List<TrackingEvent> events = trackingEventRepository.findAll();
        return events;
    }
}
