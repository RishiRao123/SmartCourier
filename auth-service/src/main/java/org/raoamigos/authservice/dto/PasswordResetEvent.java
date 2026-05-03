package org.raoamigos.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published to RabbitMQ when a user requests a password reset.
 * Consumed by notification-service to send the reset OTP email.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetEvent {
    private String email;
    private String otp;
}
