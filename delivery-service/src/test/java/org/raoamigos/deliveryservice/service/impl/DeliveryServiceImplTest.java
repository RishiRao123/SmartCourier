package org.raoamigos.deliveryservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.DeliveryUpdateEvent;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    private Delivery dummyDelivery;
    private final String TRACKING_NUMBER = "TRK12345678";
    private final Long CUSTOMER_ID = 101L;

    @BeforeEach
    void setUp() {
        dummyDelivery = Delivery.builder()
                .customerId(CUSTOMER_ID)
                .trackingNumber(TRACKING_NUMBER)
                .status(DeliveryStatus.BOOKED)
                .build();
    }

    @Test
    void createDelivery_ShouldSaveToDatabaseAndPublishEvent() {
        DeliveryRequestDTO requestDTO = new DeliveryRequestDTO();
        requestDTO.setSenderName("John Doe");
        requestDTO.setReceiverName("Jane Doe");

        DeliveryRequestDTO.AddressDTO senderAddress = new DeliveryRequestDTO.AddressDTO();
        senderAddress.setStreet("123 Sender St");
        senderAddress.setCity("Sender City");
        senderAddress.setState("TX");
        senderAddress.setZipCode("75001");
        requestDTO.setSenderAddress(senderAddress);

        DeliveryRequestDTO.AddressDTO receiverAddress = new DeliveryRequestDTO.AddressDTO();
        receiverAddress.setStreet("456 Receiver Ave");
        receiverAddress.setCity("Receiver City");
        receiverAddress.setState("NY");
        receiverAddress.setZipCode("10001");
        requestDTO.setReceiverAddress(receiverAddress);

        DeliveryRequestDTO.PackageDTO packageDetails = new DeliveryRequestDTO.PackageDTO();
        packageDetails.setWeight(2.5);
        packageDetails.setDimensions("15x15x15");
        packageDetails.setDescription("Books");
        requestDTO.setPackageDetails(packageDetails);

        when(deliveryRepository.save(any(Delivery.class))).thenReturn(dummyDelivery);

        Delivery result = deliveryService.createDelivery(requestDTO, CUSTOMER_ID);

        assertNotNull(result);
        assertEquals(DeliveryStatus.BOOKED, result.getStatus());
        assertEquals(TRACKING_NUMBER, result.getTrackingNumber());

        verify(deliveryRepository, times(1)).save(any(Delivery.class));
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("delivery.exchange"),
                eq("delivery.routing.key"),
                any(DeliveryUpdateEvent.class)
        );
    }

    @Test
    void getDeliveryByTrackingNumber_WhenNotFound_ShouldThrowException() {
        when(deliveryRepository.findByTrackingNumber(TRACKING_NUMBER)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                deliveryService.getDeliveryByTrackingNumber(TRACKING_NUMBER)
        );

        assertEquals("Delivery not found with trackingNumber : " + TRACKING_NUMBER, exception.getMessage());

        verify(deliveryRepository, times(1)).findByTrackingNumber(TRACKING_NUMBER);
    }

    @Test
    void updateDeliveryStatus_ShouldUpdateStatusSaveAndPublishEvent() {
        when(deliveryRepository.findByTrackingNumber(TRACKING_NUMBER)).thenReturn(Optional.of(dummyDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(dummyDelivery);

        Delivery result = deliveryService.updateDeliveryStatus(TRACKING_NUMBER, DeliveryStatus.IN_TRANSIT);

        assertEquals(DeliveryStatus.IN_TRANSIT, result.getStatus());

        verify(deliveryRepository, times(1)).findByTrackingNumber(TRACKING_NUMBER);
        verify(deliveryRepository, times(1)).save(dummyDelivery);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("delivery.exchange"),
                eq("delivery.routing.key"),
                any(DeliveryUpdateEvent.class)
        );
    }

    @Test
    void markAsDelivered_ShouldSetStatusToDeliveredAndPublishEvent() {
        when(deliveryRepository.findByTrackingNumber(TRACKING_NUMBER)).thenReturn(Optional.of(dummyDelivery));
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(dummyDelivery);

        Delivery result = deliveryService.markAsDelivered(TRACKING_NUMBER);

        assertEquals(DeliveryStatus.DELIVERED, result.getStatus());

        verify(deliveryRepository, times(1)).findByTrackingNumber(TRACKING_NUMBER);
        verify(deliveryRepository, times(1)).save(dummyDelivery);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("delivery.exchange"),
                eq("delivery.routing.key"),
                any(DeliveryUpdateEvent.class)
        );
    }

    @Test
    void getMyDeliveries_ShouldReturnListOfDeliveries() {
        when(deliveryRepository.findByCustomerId(CUSTOMER_ID)).thenReturn(List.of(dummyDelivery));

        List<Delivery> result = deliveryService.getMyDeliveries(CUSTOMER_ID);

        assertEquals(1, result.size());
        verify(deliveryRepository, times(1)).findByCustomerId(CUSTOMER_ID);
    }

    @Test
    void getMyActiveDeliveries_ShouldReturnNonDeliveredPackages() {
        when(deliveryRepository.findByCustomerIdAndStatusNot(CUSTOMER_ID, DeliveryStatus.DELIVERED))
                .thenReturn(List.of(dummyDelivery));

        List<Delivery> result = deliveryService.getMyActiveDeliveries(CUSTOMER_ID);

        assertEquals(1, result.size());
        verify(deliveryRepository, times(1)).findByCustomerIdAndStatusNot(CUSTOMER_ID, DeliveryStatus.DELIVERED);
    }

    @Test
    void getDeliveriesByStatus_ShouldReturnMatchingDeliveries() {
        when(deliveryRepository.findByStatus(DeliveryStatus.BOOKED)).thenReturn(List.of(dummyDelivery));

        List<Delivery> result = deliveryService.getDeliveriesByStatus(DeliveryStatus.BOOKED);

        assertEquals(1, result.size());
    }

    @Test
    void countDeliveriesByStatus_ShouldReturnTotalCount() {
        when(deliveryRepository.countByStatus(DeliveryStatus.IN_TRANSIT)).thenReturn(5L);

        long count = deliveryService.countDeliveriesByStatus(DeliveryStatus.IN_TRANSIT);

        assertEquals(5L, count);
    }

    @Test
    void getDeliveriesByCity_ShouldReturnMatchingDeliveries() {
        when(deliveryRepository.findByReceiverAddressCityIgnoreCase("New York"))
                .thenReturn(List.of(dummyDelivery));

        List<Delivery> result = deliveryService.getDeliveriesByCity("New York");

        assertEquals(1, result.size());
    }

    @Test
    void getDeliveriesByDateRange_ShouldReturnMatchingDeliveries() {
        LocalDateTime start = LocalDateTime.now().minusDays(2);
        LocalDateTime end = LocalDateTime.now();

        when(deliveryRepository.findByCreatedAtBetween(start, end))
                .thenReturn(List.of(dummyDelivery));

        List<Delivery> result = deliveryService.getDeliveriesByDateRange(start, end);

        assertEquals(1, result.size());
    }
}