package org.raoamigos.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.config.RabbitMQConfig;
import org.raoamigos.notificationservice.dto.PasswordResetEvent;
import org.raoamigos.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.PASSWORD_RESET_QUEUE)
    public void handlePasswordReset(PasswordResetEvent event) {
        log.info("🔑 [RESET] Received password reset event for email: {}", event.getEmail());
        emailService.sendPasswordResetEmail(event);
    }
}
