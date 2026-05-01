package org.raoamigos.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDTO {

    private Long id;
    private String username;
    private String email;
    private String role;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String profileImagePath;
    private boolean active;
}
