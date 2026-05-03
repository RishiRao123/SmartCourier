package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published by delivery-service when a delivery is marked as DELIVERED.
 * Contains info for a delivery confirmation email to the customer.
 * Fields MUST match delivery-service's DeliveryDeliveredEvent exactly.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDeliveredEvent {

    private String customerEmail;
    private String trackingNumber;
    private String receiverName;
    private String deliveryNote;
}
