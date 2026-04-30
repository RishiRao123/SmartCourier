package org.raoamigos.adminservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private String city;
    private String state;
    private String profileImagePath;
    private LocalDateTime createdAt;
}
