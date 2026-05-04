package org.raoamigos.deliveryservice.service;

import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.InvoiceResponseDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;

import java.time.Instant;
import java.util.List;

public interface DeliveryService {

    Delivery createDelivery(DeliveryRequestDTO deliveryRequestDTO, Long customerId, String customerEmail);
    List<Delivery> getMyDeliveries(Long customerId);
    Delivery getDeliveryByTrackingNumber(String trackingNumber);
    Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus newStatus, String proofImagePath, String deliveryNote);
    Delivery markAsDelivered(String trackingNumber, String proofImagePath, String deliveryNote);
    List<Delivery> getMyActiveDeliveries(Long customerId);
    List<Delivery> getMyDeliveredDeliveries(Long customerId);
    List<Delivery> getDeliveriesByStatus(DeliveryStatus status);
    long countDeliveriesByStatus(DeliveryStatus status);
    List<Delivery> getDeliveriesByCity(String city);
    List<Delivery> getDeliveriesByDateRange(Instant startDate, Instant endDate);
    List<Delivery> searchDeliveries(DeliveryStatus status, String city, Instant start, Instant end);
    InvoiceResponseDTO getInvoiceByTrackingNumber(String trackingNumber);
}
