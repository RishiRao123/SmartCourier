package org.raoamigos.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.config.RabbitMQConfig;
import org.raoamigos.notificationservice.dto.OtpEmailEvent;
import org.raoamigos.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listens for OTP email events and sends verification emails.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OtpEmailListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.OTP_QUEUE)
    public void handleOtpEmail(OtpEmailEvent event) {
        log.info("📧 [OTP] Received OTP event for email: {}", event.getEmail());
        emailService.sendOtpEmail(event);
    }
}
