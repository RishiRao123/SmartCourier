package org.raoamigos.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryUpdateEvent {

    private String trackingNumber;
    private String status;
    private String message;
}
