package org.raoamigos.trackingservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.trackingservice.dto.DeliveryUpdateEvent;
import org.raoamigos.trackingservice.entity.HubLocation;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingEventListener {

    private final TrackingEventRepository trackingEventRepository;

    @RabbitListener(queues = "tracking.queue")
    public void handleDeliveryUpdate(DeliveryUpdateEvent event) {
        log.info("Received new message from RabbitMQ for Tracking Number: {}", event.getTrackingNumber());

        TrackingEvent trackingEvent = TrackingEvent.builder()
                .trackingNumber(event.getTrackingNumber())
                .status(event.getStatus())
                .location(HubLocation.SYSTEM_GENERATED)
                .message(event.getMessage())
                .build();


        trackingEventRepository.save(trackingEvent);

        log.info("Successfully saved Tracking Event to database.");
    }
}
