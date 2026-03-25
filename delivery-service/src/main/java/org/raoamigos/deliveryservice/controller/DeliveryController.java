package org.raoamigos.deliveryservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.ApiResponse;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Delivery>> createDelivery(
            @Valid @RequestBody DeliveryRequestDTO requestDTO,
            @RequestHeader("X-User-Id") Long customerId) {

        Delivery newDelivery = deliveryService.createDelivery(requestDTO, customerId);
        return ResponseEntity.ok(ApiResponse.success("Delivery created successfully", newDelivery));
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<ApiResponse<Delivery>> getDelivery(@PathVariable String trackingNumber) {
        Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched successfully", delivery));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<Delivery>>> getMyDeliveries(@RequestHeader("X-User-Id") Long customerId) {
        List<Delivery> deliveries = deliveryService.getMyDeliveries(customerId);
        return ResponseEntity.ok(ApiResponse.success("User deliveries fetched successfully", deliveries));
    }
}