package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDeliveredEvent {

    private String customerEmail;
    private String trackingNumber;
    private String receiverName;
    private String deliveryNote;
}
