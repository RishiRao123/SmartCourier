package org.raoamigos.deliveryservice.service;

import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;

import java.util.List;

public interface DeliveryService {

    Delivery createDelivery(DeliveryRequestDTO deliveryRequestDTO, Long customerId);
    List<Delivery> getMyDeliveries(Long customerId);
    Delivery getDeliveryByTrackingNumber(String trackingNumber);
}
