package org.raoamigos.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDeliveredEvent implements Serializable {
    private String customerEmail;
    private String trackingNumber;
    private String receiverName;
    private String deliveryNote;
}
