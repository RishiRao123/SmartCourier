package org.raoamigos.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {

    private String invoiceNumber;
    private String trackingNumber;
    private Double amount;
    private Double weightKg;
    private String paymentMethod;
    private String paymentStatus;
    private String senderName;
    private String receiverName;
    private String receiverCity;
    private String description;
    private Instant createdAt;
    private Instant paidAt;
}
