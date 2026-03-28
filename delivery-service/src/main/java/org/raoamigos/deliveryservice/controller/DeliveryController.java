package org.raoamigos.deliveryservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.ApiResponse;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Delivery>> createDelivery(
            @Valid @RequestBody DeliveryRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long customerId) {
        Delivery newDelivery = deliveryService.createDelivery(requestDTO, customerId);
        return ResponseEntity.ok(ApiResponse.success("Delivery created successfully", newDelivery));
    }

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> getDelivery(@PathVariable String trackingNumber) {
        Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched successfully", delivery));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getMyDeliveries(@RequestHeader("X-User-Id") Long customerId) {
        List<Delivery> deliveries = deliveryService.getMyDeliveries(customerId);
        return ResponseEntity.ok(ApiResponse.success("User deliveries fetched successfully", deliveries));
    }

    @PutMapping("/{trackingNumber}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(@PathVariable String trackingNumber, @RequestParam("status") DeliveryStatus newStatus) {
        Delivery updateDelivery = deliveryService.updateDeliveryStatus(trackingNumber, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updateDelivery));
    }

    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalDeliveries() {
        long count = deliveryRepository.count();
        return ResponseEntity.ok(ApiResponse.success("Total deliveries fetched", count));
    }

    @PutMapping("/{trackingNumber}/deliver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> markDelivered(@PathVariable String trackingNumber) {
        Delivery updatedDelivery = deliveryService.markAsDelivered(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Package marked as delivered", updatedDelivery));
    }
}