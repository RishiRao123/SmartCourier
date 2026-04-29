package org.raoamigos.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.client.TrackingClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.raoamigos.adminservice.dto.TrackingEventDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
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

    @GetMapping("/dashboard/deliveries/status/{status}")
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByStatus(@PathVariable String status) {
        return ResponseEntity.ok(deliveryClient.getDeliveriesByStatus(status));
    }

    @GetMapping("/dashboard/deliveries/city/{city}")
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveriesByCity(@PathVariable String city) {
        return ResponseEntity.ok(deliveryClient.getDeliveriesByCity(city));
    }

    @GetMapping("/dashboard/deliveries/report")
    public ResponseEntity<ApiResponse<List<DeliveryDTO>>> getDeliveryReport(
            @RequestParam String start,
            @RequestParam String end) {
        return ResponseEntity.ok(deliveryClient.getDeliveriesByDateRange(start, end));
    }


    @GetMapping("/dashboard/tracking/{trackingNumber}/history")
    public ResponseEntity<List<TrackingEventDTO>> getFullTrackingHistory(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingClient.getTrackingHistory(trackingNumber).getData());
    }

    @GetMapping("/dashboard/tracking/recent")
    public ResponseEntity<List<TrackingEventDTO>> getRecentTrackingEvents(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(trackingClient.getRecentSystemEvents(days));
    }

    @GetMapping("/dashboard/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMasterDashboardSummary() {
        Map<String, Object> summary = new HashMap<>();

        summary.put("totalDeliveries", deliveryClient.getTotalDeliveries().getData());
        summary.put("activeInTransit", deliveryClient.countDeliveriesByStatus("IN_TRANSIT").getData());
        summary.put("totalDelivered", deliveryClient.countDeliveriesByStatus("DELIVERED").getData());
        summary.put("recentEvents", trackingClient.getRecentSystemEvents(1).size()); // Events in last 24h

        return ResponseEntity.ok(ApiResponse.success("Master Dashboard Summary fetched", summary));
    }
}