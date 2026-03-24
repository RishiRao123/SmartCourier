package org.raoamigos.deliveryservice.repository;

import org.raoamigos.deliveryservice.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    List<Delivery> findByCustomerId(Long customerId);
}
