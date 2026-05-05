package org.raoamigos.notificationservice.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.notificationservice.config.RabbitMQConfig;
import org.raoamigos.notificationservice.dto.AdminCredentialsEvent;
import org.raoamigos.notificationservice.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class AdminCredentialsListener {

    private final EmailService emailService;

    @RabbitListener(queues = RabbitMQConfig.ADMIN_CREDENTIALS_QUEUE)
    public void handleAdminCredentials(AdminCredentialsEvent event) {
        log.info("📧 [ADMIN] Received admin credentials event for email: {}", event.getEmail());
        emailService.sendAdminCredentialsEmail(event);
    }
}
