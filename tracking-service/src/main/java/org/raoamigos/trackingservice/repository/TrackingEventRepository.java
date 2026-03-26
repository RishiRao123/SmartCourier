package org.raoamigos.trackingservice.repository;

import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByTrackingNumberOrderByTimestampDesc(String travkingNumber);
}
