package org.raoamigos.deliveryservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.ApiResponse;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.InvoiceResponseDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> getDelivery(@PathVariable String trackingNumber) {
        Delivery delivery = deliveryService.getDeliveryByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Delivery fetched successfully", delivery));
    }

    @GetMapping("/{trackingNumber}/invoice")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponseDTO>> getInvoice(@PathVariable String trackingNumber) {
        InvoiceResponseDTO invoice = deliveryService.getInvoiceByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Invoice fetched successfully", invoice));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getMyDeliveries(@RequestHeader("X-User-Id") Long customerId) {
        List<Delivery> deliveries = deliveryService.getMyDeliveries(customerId);
        return ResponseEntity.ok(ApiResponse.success("User deliveries fetched successfully", deliveries));
    }

    @PutMapping("/{trackingNumber}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> updateStatus(
            @PathVariable String trackingNumber, 
            @RequestParam("status") DeliveryStatus newStatus,
            @RequestParam(value = "proofImagePath", required = false) String proofImagePath,
            @RequestParam(value = "deliveryNote", required = false) String deliveryNote) {
        Delivery updateDelivery = deliveryService.updateDeliveryStatus(trackingNumber, newStatus, proofImagePath, deliveryNote);
        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", updateDelivery));
    }

    @GetMapping("/stats/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getTotalDeliveries() {
        long count = deliveryRepository.count();
        return ResponseEntity.ok(ApiResponse.success("Total deliveries fetched", count));
    }

    @PutMapping("/{trackingNumber}/deliver")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Delivery>> markDelivered(
            @PathVariable String trackingNumber,
            @RequestParam(value = "proofImagePath", required = true) String proofImagePath,
            @RequestParam(value = "deliveryNote", required = false) String deliveryNote) {
        Delivery updatedDelivery = deliveryService.markAsDelivered(trackingNumber, proofImagePath, deliveryNote);
        return ResponseEntity.ok(ApiResponse.success("Package marked as delivered", updatedDelivery));
    }

    @GetMapping("/my/active")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getMyActiveDeliveries(@RequestHeader("X-User-Id") Long customerId) {
        List<Delivery> activeDeliveries = deliveryService.getMyActiveDeliveries(customerId);
        return ResponseEntity.ok(ApiResponse.success("Active deliveries fetched", activeDeliveries));
    }

    @GetMapping("/my/delivered")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getMyDeliveredDeliveries(@RequestHeader("X-User-Id") Long customerId) {
        List<Delivery> deliveredDeliveries = deliveryService.getMyDeliveredDeliveries(customerId);
        return ResponseEntity.ok(ApiResponse.success("Delivered deliveries fetched", deliveredDeliveries));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getByStatus(@PathVariable DeliveryStatus status) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Deliveries fetched by status", deliveries));
    }

    @GetMapping("/admin/status/{status}/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> countByStatus(@PathVariable DeliveryStatus status) {
        long count = deliveryService.countDeliveriesByStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Count fetched", count));
    }

    @GetMapping("/admin/city/{city}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getByCity(@PathVariable String city) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByCity(city);
        return ResponseEntity.ok(ApiResponse.success("Deliveries to " + city + " fetched", deliveries));
    }

    @GetMapping("/admin/report")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Delivery>>> getByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Delivery> deliveries = deliveryService.getDeliveriesByDateRange(start, end);
        return ResponseEntity.ok(ApiResponse.success("Deliveries in date range fetched", deliveries));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Delivery>>> searchDeliveries(
            @RequestParam(required = false) DeliveryStatus status,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Delivery> deliveries = deliveryService.searchDeliveries(status, city, start, end);
        return ResponseEntity.ok(ApiResponse.success("Deliveries searched successfully", deliveries));
    }
}