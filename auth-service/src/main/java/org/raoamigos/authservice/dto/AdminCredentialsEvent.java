package org.raoamigos.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCredentialsEvent implements Serializable {
    private String email;
    private String username;
    private String rawPassword;
}
