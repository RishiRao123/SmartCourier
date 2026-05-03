package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published by auth-service when a user signs up.
 * Contains the OTP code and the user's email for verification.
 * 
 * Field names MUST match auth-service's OtpEvent exactly for JSON deserialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpEmailEvent {

    private String email;
    private String otp;
}
