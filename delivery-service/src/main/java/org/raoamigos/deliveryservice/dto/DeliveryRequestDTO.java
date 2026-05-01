package org.raoamigos.deliveryservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeliveryRequestDTO {

    @NotBlank(message = "Sender name is required")
    private String senderName;

    @NotNull(message = "Sender address is required")
    private AddressDTO senderAddress;

    @NotBlank(message = "Receiver name is required")
    private String receiverName;

    @NotBlank(message = "Receiver phone is required")
    private String receiverPhone;

    @NotNull(message = "Receiver address is required")
    private AddressDTO receiverAddress;

    @NotNull(message = "Package details are required")
    private PackageDTO packageDetails;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // "PAY_ON_DELIVERY" or "PAY_NOW"

    @Data
    public static class AddressDTO {
        @NotBlank private String street;
        @NotBlank private String city;
        @NotBlank private String state;
        @NotBlank private String zipCode;
    }

    @Data
    public static class PackageDTO {
        private Double weight;
        @NotBlank private String description;
    }
}
