package org.raoamigos.trackingservice.repository;

import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {

    List<TrackingEvent> findByTrackingNumberOrderByTimestampDesc(String travkingNumber);

    Optional<TrackingEvent> findFirstByTrackingNumberOrderByTimestampDesc(String trackingNumber);

    List<TrackingEvent> findByStatusOrderByTimestampDesc(String status);

    List<TrackingEvent> findByTimestampAfterOrderByTimestampDesc(LocalDateTime timestamp);

    long countByTrackingNumber(String trackingNumber);
}
