package org.raoamigos.authservice.dto;

import lombok.Data;

@Data
public class ProfileUpdateDTO {

    private String username;
    private String phone;
    private String street;
    private String city;
    private String state;
    private String zipCode;
}
