package org.raoamigos.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private String city;
    private String state;
    private String profileImagePath;
    private Instant createdAt;
    private boolean active;
}
