package org.raoamigos.deliveryservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    public ResponseEntity<Delivery> createDelivery(@Valid @RequestBody DeliveryRequestDTO delivery,
                                                   @RequestHeader("X-User-Id") Long customerId
                                                   ) {

        return ResponseEntity.ok( deliveryService.createDelivery(delivery, customerId));
    }

    @GetMapping("/{trackingNumber}")
    public ResponseEntity<Delivery> getDeliveryByTrackingNumber(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(deliveryService.getDeliveryByTrackingNumber(trackingNumber));
    }

}
