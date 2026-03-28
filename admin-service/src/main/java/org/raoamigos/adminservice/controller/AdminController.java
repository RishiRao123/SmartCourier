package org.raoamigos.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.client.TrackingClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final DeliveryClient deliveryClient;
    private final TrackingClient trackingClient;

    @GetMapping("/deliveries/{trackingNumber}")
    public ResponseEntity<ApiResponse<DeliveryDTO>> fetchDeliveryFromOtherService(@PathVariable String trackingNumber) {
        ApiResponse<DeliveryDTO> response = deliveryClient.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Successfully fetched cross-service data", response.getData()));
    }

    @PutMapping("/deliveries/{trackingNumber}/resolve")
    public ResponseEntity<ApiResponse<DeliveryDTO>> resolveDeliveryException(@PathVariable String trackingNumber, @RequestParam String newStatus) {
        ApiResponse<DeliveryDTO> updatedDelivery = deliveryClient.updateDeliveryStatus(trackingNumber, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Exception resolved successfully", updatedDelivery.getData()));
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboardStats() {
        ApiResponse<Long> deliveryResponse = deliveryClient.getTotalDeliveries();
        ApiResponse<Long> trackingResponse = trackingClient.getTotalTrackingEvents();
        Map<String, Long> dashboardData = new HashMap<>();
        dashboardData.put("totalActiveDeliveries", deliveryResponse.getData());
        dashboardData.put("totalTrackingEvents", trackingResponse.getData());

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats aggregated successfully", dashboardData));
    }

    @PutMapping("/deliveries/{trackingNumber}/deliver")
    public ResponseEntity<ApiResponse<DeliveryDTO>> markDeliveryComplete(@PathVariable String trackingNumber) {
        ApiResponse<DeliveryDTO> response = deliveryClient.markDelivered(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Admin successfully finalized delivery", response.getData()));
    }
}