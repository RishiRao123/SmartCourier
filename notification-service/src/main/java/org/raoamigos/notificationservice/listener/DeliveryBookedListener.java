package org.raoamigos.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.config.RabbitMQConfig;
import org.raoamigos.notificationservice.dto.DeliveryBookedEvent;
import org.raoamigos.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryBookedListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_BOOKED_QUEUE)
    public void handleDeliveryBooked(DeliveryBookedEvent event) {
        log.info("📧 [BOOKED] Received delivery booked event — Tracking: {}", event.getTrackingNumber());
        emailService.sendDeliveryBookedEmail(event);
    }
}
