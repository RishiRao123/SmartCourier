package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredentialsEvent {

    private String email;
    private String username;
    private String rawPassword;
}
