package org.raoamigos.deliveryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.DeliveryUpdateEvent;
import org.raoamigos.deliveryservice.entity.Address;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.entity.PackageDetails;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public Delivery createDelivery(DeliveryRequestDTO dto, Long customerId) {

        String trackingNumber = "TRK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Address senderAddress = Address.builder()
                .street(dto.getSenderAddress().getStreet())
                .city(dto.getSenderAddress().getCity())
                .state(dto.getSenderAddress().getState())
                .zipCode(dto.getSenderAddress().getZipCode())
                .build();

        Address receiverAddress = Address.builder()
                .street(dto.getReceiverAddress().getStreet())
                .city(dto.getReceiverAddress().getCity())
                .state(dto.getReceiverAddress().getState())
                .zipCode(dto.getReceiverAddress().getZipCode())
                .build();

        PackageDetails packageDetails = PackageDetails.builder()
                .weight(dto.getPackageDetails().getWeight())
                .dimensions(dto.getPackageDetails().getDimensions())
                .description(dto.getPackageDetails().getDescription())
                .build();

        Delivery delivery = Delivery.builder()
                .customerId(customerId)
                .trackingNumber(trackingNumber)
                .senderName(dto.getSenderName())
                .senderAddress(senderAddress)
                .receiverAddress(receiverAddress)
                .receiverName(dto.getReceiverName())
                .packageDetails(packageDetails)
                .status(DeliveryStatus.BOOKED)
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                saved.getTrackingNumber(),
                saved.getStatus().name(),
                "Delivery request created and booked successfully."
        );

        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        return saved;
    }

    @Override
    public Delivery getDeliveryByTrackingNumber(String trackingNumber) {
        return deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Delivery not found with trackingNumber : " + trackingNumber));
    }

    @Override
    public List<Delivery> getMyDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerId(customerId);
    }

    @Override
    public Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus newStatus) {
        Delivery delivery = getDeliveryByTrackingNumber(trackingNumber);
        delivery.setStatus(newStatus);

        Delivery saved = deliveryRepository.save(delivery);

        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                saved.getTrackingNumber(),
                saved.getStatus().name(),
                "Delivery status manually updated by Admin to: " + newStatus.name()
        );

        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        return saved;

    }

    @Override
    public Delivery markAsDelivered(String trackingNumber) {
        Delivery delivery = getDeliveryByTrackingNumber(trackingNumber);

        delivery.setStatus(DeliveryStatus.DELIVERED);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                savedDelivery.getTrackingNumber(),
                savedDelivery.getStatus().name(),
                "Package has been successfully delivered to the customer."
        );
        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        return savedDelivery;
    }

    @Override
    public List<Delivery> getMyActiveDeliveries(Long customerId) {
        // Fetch all deliveries for this customer that are NOT marked as DELIVERED
        return deliveryRepository.findByCustomerIdAndStatusNot(customerId, DeliveryStatus.DELIVERED);
    }

    @Override
    public List<Delivery> getDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.findByStatus(status);
    }

    @Override
    public long countDeliveriesByStatus(DeliveryStatus status) {
        return deliveryRepository.countByStatus(status);
    }

    @Override
    public List<Delivery> getDeliveriesByCity(String city) {
        return deliveryRepository.findByReceiverAddressCityIgnoreCase(city);
    }

    @Override
    public List<Delivery> getDeliveriesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return deliveryRepository.findByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<Delivery> searchDeliveries(DeliveryStatus status, String city, LocalDateTime start, LocalDateTime end) {
        List<Delivery> all = deliveryRepository.findAll();
        
        return all.stream()
                .filter(d -> status == null || d.getStatus() == status)
                .filter(d -> city == null || city.isEmpty() || 
                        d.getReceiverAddress().getCity().equalsIgnoreCase(city) || 
                        d.getSenderAddress().getCity().equalsIgnoreCase(city))
                .filter(d -> start == null || d.getCreatedAt().isAfter(start))
                .filter(d -> end == null || d.getCreatedAt().isBefore(end))
                .toList();
    }
}
