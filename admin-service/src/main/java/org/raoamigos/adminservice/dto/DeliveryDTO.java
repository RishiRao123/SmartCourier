package org.raoamigos.adminservice.dto;

import lombok.Data;

@Data
public class DeliveryDTO {

    private Long id;
    private String trackingNumber;
    private String senderName;
    private String receiverName;
    private String status;
}
