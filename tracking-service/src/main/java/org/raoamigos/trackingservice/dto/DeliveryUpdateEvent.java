package org.raoamigos.trackingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryUpdateEvent {
    private String trackingNumber;
    private String status;
    private String message;
}