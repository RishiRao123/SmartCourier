package org.raoamigos.deliveryservice.service;

import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryService {

    Delivery createDelivery(DeliveryRequestDTO deliveryRequestDTO, Long customerId);
    List<Delivery> getMyDeliveries(Long customerId);
    Delivery getDeliveryByTrackingNumber(String trackingNumber);
    Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus newStatus);
    Delivery markAsDelivered(String trackingNumber);
    List<Delivery> getMyActiveDeliveries(Long customerId);
    List<Delivery> getDeliveriesByStatus(DeliveryStatus status);
    long countDeliveriesByStatus(DeliveryStatus status);
    List<Delivery> getDeliveriesByCity(String city);
    List<Delivery> getDeliveriesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}
