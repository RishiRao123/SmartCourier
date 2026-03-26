package org.raoamigos.trackingservice.service;

import org.raoamigos.trackingservice.entity.TrackingEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface TrackingService {

    public List<TrackingEvent> getTrackingHistory(String trackingNumber);

    public List<TrackingEvent> getAllTrackings();
}
