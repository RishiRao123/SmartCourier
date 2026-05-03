package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event consumed from RabbitMQ when a user requests a password reset.
 * Fields MUST match auth-service's PasswordResetEvent exactly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent {
    private String email;
    private String otp;
}
