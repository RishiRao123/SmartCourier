package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published by auth-service when a Super Admin creates a new Admin.
 * The system generates a random password and emails it to the new admin.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredentialsEvent {

    private String email;
    private String username;
    private String rawPassword;
}
