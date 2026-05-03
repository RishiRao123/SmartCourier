package org.raoamigos.deliveryservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.DeliveryUpdateEvent;
import org.raoamigos.deliveryservice.dto.InvoiceResponseDTO;
import org.raoamigos.deliveryservice.entity.*;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.repository.InvoiceRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.raoamigos.deliveryservice.service.PricingService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final InvoiceRepository invoiceRepository;
    private final PricingService pricingService;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional
    public Delivery createDelivery(DeliveryRequestDTO dto, Long customerId, String customerEmail) {

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
                .description(dto.getPackageDetails().getDescription())
                .build();

        // Calculate price from the pricing engine
        double price = pricingService.calculatePrice(dto.getPackageDetails().getWeight());

        // Determine payment method and status
        PaymentMethod paymentMethod = PaymentMethod.valueOf(dto.getPaymentMethod());
        PaymentStatus paymentStatus = (paymentMethod == PaymentMethod.PAY_NOW)
                ? PaymentStatus.PAID
                : PaymentStatus.UNPAID;

        Delivery delivery = Delivery.builder()
                .customerId(customerId)
                .customerEmail(customerEmail)
                .trackingNumber(trackingNumber)
                .senderName(dto.getSenderName())
                .senderAddress(senderAddress)
                .receiverAddress(receiverAddress)
                .receiverName(dto.getReceiverName())
                .receiverPhone(dto.getReceiverPhone())
                .packageDetails(packageDetails)
                .price(price)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .status(DeliveryStatus.BOOKED)
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        // Create Invoice
        String invoiceNumber = "INV" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .delivery(saved)
                .amount(price)
                .weightKg(dto.getPackageDetails().getWeight())
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .paidAt(paymentMethod == PaymentMethod.PAY_NOW ? LocalDateTime.now() : null)
                .build();

        invoiceRepository.save(invoice);

        // Publish event to RabbitMQ
        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                saved.getTrackingNumber(),
                saved.getStatus().name(),
                "Delivery booked. Invoice " + invoiceNumber + " generated. Amount: ₹" + price,
                null,
                null
        );
        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        // Publish Booked Notification Event
        if (customerEmail != null && !customerEmail.isEmpty()) {
            org.raoamigos.deliveryservice.dto.DeliveryBookedEvent bookedEvent = new org.raoamigos.deliveryservice.dto.DeliveryBookedEvent(
                    customerEmail,
                    saved.getSenderName(),
                    saved.getTrackingNumber(),
                    saved.getReceiverName(),
                    saved.getReceiverAddress().getCity(),
                    saved.getPrice(),
                    saved.getPaymentMethod().name()
            );
            rabbitTemplate.convertAndSend(org.raoamigos.deliveryservice.config.RabbitMQConfig.NOTIFICATION_EXCHANGE, 
                                          org.raoamigos.deliveryservice.config.RabbitMQConfig.DELIVERY_BOOKED_ROUTING_KEY, 
                                          bookedEvent);
        }

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

    @Transactional
    @Override
    public Delivery updateDeliveryStatus(String trackingNumber, DeliveryStatus newStatus, String proofImagePath, String deliveryNote) {
        Delivery delivery = getDeliveryByTrackingNumber(trackingNumber);
        delivery.setStatus(newStatus);
        if (newStatus == DeliveryStatus.DELIVERED) {
            if (delivery.getPaymentMethod() == PaymentMethod.PAY_ON_DELIVERY) {
                delivery.setPaymentStatus(PaymentStatus.PAID);
                invoiceRepository.findByDeliveryTrackingNumber(trackingNumber).ifPresent(invoice -> {
                    invoice.setPaymentStatus(PaymentStatus.PAID);
                    if (invoice.getPaidAt() == null) {
                        invoice.setPaidAt(LocalDateTime.now());
                    }
                    invoiceRepository.save(invoice);
                });
            }
        }

        Delivery saved = deliveryRepository.save(delivery);

        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                saved.getTrackingNumber(),
                saved.getStatus().name(),
                "Delivery status manually updated by Admin to: " + newStatus.name(),
                proofImagePath,
                deliveryNote
        );
        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        if (newStatus == DeliveryStatus.DELIVERED && saved.getCustomerEmail() != null) {
            org.raoamigos.deliveryservice.dto.DeliveryDeliveredEvent deliveredEvent = new org.raoamigos.deliveryservice.dto.DeliveryDeliveredEvent(
                    saved.getCustomerEmail(),
                    saved.getTrackingNumber(),
                    saved.getReceiverName(),
                    deliveryNote
            );
            rabbitTemplate.convertAndSend(org.raoamigos.deliveryservice.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                          org.raoamigos.deliveryservice.config.RabbitMQConfig.DELIVERY_DELIVERED_ROUTING_KEY,
                                          deliveredEvent);
        }

        return saved;
    }

    @Transactional
    @Override
    public Delivery markAsDelivered(String trackingNumber, String proofImagePath, String deliveryNote) {
        Delivery delivery = getDeliveryByTrackingNumber(trackingNumber);
        delivery.setStatus(DeliveryStatus.DELIVERED);
        
        if (delivery.getPaymentMethod() == PaymentMethod.PAY_ON_DELIVERY) {
            delivery.setPaymentStatus(PaymentStatus.PAID);
            invoiceRepository.findByDeliveryTrackingNumber(trackingNumber).ifPresent(invoice -> {
                invoice.setPaymentStatus(PaymentStatus.PAID);
                if (invoice.getPaidAt() == null) {
                    invoice.setPaidAt(LocalDateTime.now());
                }
                invoiceRepository.save(invoice);
            });
        }

        Delivery savedDelivery = deliveryRepository.save(delivery);

        DeliveryUpdateEvent event = new DeliveryUpdateEvent(
                savedDelivery.getTrackingNumber(),
                savedDelivery.getStatus().name(),
                "Package has been successfully delivered to the customer.",
                proofImagePath,
                deliveryNote
        );
        rabbitTemplate.convertAndSend("delivery.exchange", "delivery.routing.key", event);

        if (savedDelivery.getCustomerEmail() != null) {
            org.raoamigos.deliveryservice.dto.DeliveryDeliveredEvent deliveredEvent = new org.raoamigos.deliveryservice.dto.DeliveryDeliveredEvent(
                    savedDelivery.getCustomerEmail(),
                    savedDelivery.getTrackingNumber(),
                    savedDelivery.getReceiverName(),
                    deliveryNote
            );
            rabbitTemplate.convertAndSend(org.raoamigos.deliveryservice.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                          org.raoamigos.deliveryservice.config.RabbitMQConfig.DELIVERY_DELIVERED_ROUTING_KEY,
                                          deliveredEvent);
        }

        return savedDelivery;
    }

    @Override
    public List<Delivery> getMyActiveDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerIdAndStatusNot(customerId, DeliveryStatus.DELIVERED);
    }

    @Override
    public List<Delivery> getMyDeliveredDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerIdAndStatus(customerId, DeliveryStatus.DELIVERED);
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

    @Override
    @Transactional
    public InvoiceResponseDTO getInvoiceByTrackingNumber(String trackingNumber) {
        Invoice invoice = invoiceRepository.findByDeliveryTrackingNumber(trackingNumber)
                .orElseGet(() -> {
                    Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                            .orElseThrow(() -> new RuntimeException("Delivery not found for tracking: " + trackingNumber));

                    Invoice newInvoice = Invoice.builder()
                            .invoiceNumber("INV-" + System.currentTimeMillis())
                            .delivery(delivery)
                            .amount(delivery.getPrice() != null ? delivery.getPrice() : pricingService.calculatePrice(delivery.getPackageDetails().getWeight()))
                            .weightKg(delivery.getPackageDetails().getWeight())
                            .paymentMethod(delivery.getPaymentMethod() != null ? delivery.getPaymentMethod() : org.raoamigos.deliveryservice.entity.PaymentMethod.PAY_ON_DELIVERY)
                            .paymentStatus(delivery.getPaymentStatus() != null ? delivery.getPaymentStatus() : org.raoamigos.deliveryservice.entity.PaymentStatus.UNPAID)
                            .createdAt(LocalDateTime.now())
                            .build();
                    return invoiceRepository.save(newInvoice);
                });

        Delivery delivery = invoice.getDelivery();

        return InvoiceResponseDTO.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .trackingNumber(delivery.getTrackingNumber())
                .amount(invoice.getAmount())
                .weightKg(invoice.getWeightKg())
                .paymentMethod(invoice.getPaymentMethod().name())
                .paymentStatus(invoice.getPaymentStatus().name())
                .senderName(delivery.getSenderName())
                .receiverName(delivery.getReceiverName())
                .receiverCity(delivery.getReceiverAddress().getCity())
                .description(delivery.getPackageDetails().getDescription())
                .createdAt(invoice.getCreatedAt())
                .paidAt(invoice.getPaidAt())
                .build();
    }
}
