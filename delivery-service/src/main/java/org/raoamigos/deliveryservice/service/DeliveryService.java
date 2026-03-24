package org.raoamigos.deliveryservice.service;

import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;

public interface DeliveryService {

    Delivery createDelivery(DeliveryRequestDTO deliveryRequestDTO, Long customerId);
}
