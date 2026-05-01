package org.raoamigos.adminservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.adminservice.client.AuthClient;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.client.TrackingClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.raoamigos.adminservice.dto.TrackingEventDTO;
import org.raoamigos.adminservice.dto.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminController {

    private final DeliveryClient deliveryClient;
    private final TrackingClient trackingClient;
    private final AuthClient authClient;

    // ===== Delivery Endpoints =====

    @GetMapping("/deliveries/{trackingNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> fetchDeliveryFromOtherService(@PathVariable String trackingNumber) {
        ApiResponse<DeliveryDTO> response = deliveryClient.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Successfully fetched cross-service data", response.getData()));
    }

    @PutMapping("/deliveries/{trackingNumber}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> resolveDeliveryException(
            @PathVariable String trackingNumber, 
            @RequestParam String newStatus,
            @RequestParam(required = false) String proofImagePath,
            @RequestParam(required = false) String deliveryNote) {
        ApiResponse<DeliveryDTO> updatedDelivery = deliveryClient.updateDeliveryStatus(trackingNumber, newStatus, proofImagePath, deliveryNote);
        return ResponseEntity.ok(ApiResponse.success("Exception resolved successfully", updatedDelivery.getData()));
    }

    @PutMapping("/deliveries/{trackingNumber}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<DeliveryDTO>> markDeliveryComplete(
            @PathVariable String trackingNumber,
            @RequestParam String proofImagePath,
            @RequestParam(required = false) String deliveryNote) {
        ApiResponse<DeliveryDTO> response = deliveryClient.markDelivered(trackingNumber, proofImagePath, deliveryNote);
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

    // ===== Dashboard Stats =====

    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboardStats() {
        ApiResponse<Long> deliveryResponse = deliveryClient.getTotalDeliveries();
        ApiResponse<Long> trackingResponse = trackingClient.getTotalTrackingEvents();
        Map<String, Long> dashboardData = new HashMap<>();
        dashboardData.put("totalActiveDeliveries", deliveryResponse.getData());
        dashboardData.put("totalTrackingEvents", trackingResponse.getData());

        return ResponseEntity.ok(ApiResponse.success("Dashboard stats aggregated successfully", dashboardData));
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

    // ===== Tracking Endpoints =====

    @GetMapping("/dashboard/tracking/{trackingNumber}/history")
    public ResponseEntity<ApiResponse<List<TrackingEventDTO>>> getFullTrackingHistory(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingClient.getTrackingHistory(trackingNumber));
    }

    @GetMapping("/dashboard/tracking/recent")
    public ResponseEntity<List<TrackingEventDTO>> getRecentTrackingEvents(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(trackingClient.getRecentSystemEvents(days));
    }

    // ===== User Management Endpoints =====

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(@RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(authClient.getAllUsers(role));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(authClient.getUserById(id, role));
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestParam String newRole,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(authClient.updateUserRole(id, newRole, role));
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id, @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(authClient.deleteUser(id, role));
    }

    @PutMapping("/users/{id}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> toggleUserActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(authClient.toggleUserActive(id, active, role));
    }
}