package org.raoamigos.deliveryservice.repository;

import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    List<Delivery> findByCustomerId(Long customerId);

    List<Delivery> findByCustomerIdAndStatusNot(Long customerId, DeliveryStatus status);

    List<Delivery> findByStatus(DeliveryStatus status);

    long countByStatus(DeliveryStatus status);

    List<Delivery> findByReceiverAddressCityIgnoreCase(String city);

    List<Delivery> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
