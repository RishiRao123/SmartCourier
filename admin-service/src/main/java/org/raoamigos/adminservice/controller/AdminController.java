package org.raoamigos.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
