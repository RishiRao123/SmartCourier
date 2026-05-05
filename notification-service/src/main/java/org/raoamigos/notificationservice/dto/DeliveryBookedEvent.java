package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryBookedEvent {

    private String customerEmail;
    private String customerName;
    private String trackingNumber;
    private String receiverName;
    private String receiverCity;
    private Double price;
    private String paymentMethod;
}
