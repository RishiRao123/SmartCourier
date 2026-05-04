package org.raoamigos.trackingservice.service;

import org.raoamigos.trackingservice.entity.TrackingEvent;

import java.util.List;

public interface TrackingService {

    public List<TrackingEvent> getTrackingHistory(String trackingNumber);
    void saveTrackingEvent(TrackingEvent event);
    List<TrackingEvent> getFullHistory(String trackingNumber);
    TrackingEvent getLatestEvent(String trackingNumber);
    List<TrackingEvent> getEventsByStatus(String status);
    List<TrackingEvent> getRecentSystemEvents(int days);
    long getUpdateCount(String trackingNumber);
    public List<TrackingEvent> getAllTrackings();
}
