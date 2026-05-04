package org.raoamigos.trackingservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.raoamigos.trackingservice.service.TrackingService;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
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
    public void saveTrackingEvent(TrackingEvent event) {
        trackingEventRepository.save(event);
        log.info("Saved tracking event: trackingNumber={}, status={}", event.getTrackingNumber(), event.getStatus());
    }

    @Override
    public List<TrackingEvent> getFullHistory(String trackingNumber) {
        return trackingEventRepository.findByTrackingNumberOrderByTimestampDesc(trackingNumber);
    }

    @Override
    public TrackingEvent getLatestEvent(String trackingNumber) {
        return trackingEventRepository.findFirstByTrackingNumberOrderByTimestampDesc(trackingNumber)
                .orElseThrow(() -> new RuntimeException("No tracking history found for: " + trackingNumber));
    }

    @Override
    public List<TrackingEvent> getEventsByStatus(String status) {
        return trackingEventRepository.findByStatusOrderByTimestampDesc(status);
    }

    @Override
    public List<TrackingEvent> getRecentSystemEvents(int days) {
        Instant cutoffDate = Instant.now().minus(days, ChronoUnit.DAYS);
        return trackingEventRepository.findByTimestampAfterOrderByTimestampDesc(cutoffDate);
    }

    @Override
    public long getUpdateCount(String trackingNumber) {
        return trackingEventRepository.countByTrackingNumber(trackingNumber);
    }

    @Override
    public List<TrackingEvent> getAllTrackings() {
        List<TrackingEvent> events = trackingEventRepository.findAll();
        return events;
    }
}
