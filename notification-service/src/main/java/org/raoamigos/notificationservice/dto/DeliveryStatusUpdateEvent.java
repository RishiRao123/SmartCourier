package org.raoamigos.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeliveryStatusUpdateEvent {
    private String customerEmail;
    private String customerName;
    private String trackingNumber;
    private String newStatus;
    private String location;
}
