package org.raoamigos.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.config.RabbitMQConfig;
import org.raoamigos.notificationservice.dto.DeliveryDeliveredEvent;
import org.raoamigos.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryDeliveredListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.DELIVERY_DELIVERED_QUEUE)
    public void handleDeliveryDelivered(DeliveryDeliveredEvent event) {
        log.info("📧 [DELIVERED] Received delivery delivered event — Tracking: {}", event.getTrackingNumber());
        emailService.sendDeliveryDeliveredEmail(event);
    }
}
