package org.raoamigos.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final DeliveryClient deliveryClient;

    @GetMapping("/deliveries/{trackingNumber}")
    public ResponseEntity<ApiResponse<DeliveryDTO>> fetchDeliveryFromOtherService(@PathVariable String trackingNumber) {
        ApiResponse<DeliveryDTO> response = deliveryClient.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Successfully fetched cross-service data", response.getData()));
    }

    @PutMapping("/deliveries/{trackingNumber}/resolve")
    public ResponseEntity<ApiResponse<DeliveryDTO>> resolveDeliveryException(@PathVariable String trackingNumber, @RequestParam String status) {
        ApiResponse<DeliveryDTO> updatedDelivery = deliveryClient.updateDeliveryStatus(trackingNumber, status);
        return ResponseEntity.ok(ApiResponse.success("Exception resolved successfully", updatedDelivery.getData()));
    }


}
