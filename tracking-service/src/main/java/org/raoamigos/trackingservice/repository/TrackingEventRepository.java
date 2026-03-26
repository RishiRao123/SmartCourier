package org.raoamigos.trackingservice.repository;

import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
}
