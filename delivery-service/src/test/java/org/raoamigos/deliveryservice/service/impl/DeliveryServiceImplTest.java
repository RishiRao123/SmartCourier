package org.raoamigos.deliveryservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.deliveryservice.config.RabbitMQConfig;
import org.raoamigos.deliveryservice.dto.DeliveryBookedEvent;
import org.raoamigos.deliveryservice.dto.DeliveryDeliveredEvent;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.DeliveryUpdateEvent;
import org.raoamigos.deliveryservice.dto.InvoiceResponseDTO;
import org.raoamigos.deliveryservice.entity.*;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.repository.InvoiceRepository;
import org.raoamigos.deliveryservice.service.PricingService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Phase 3 — DeliveryServiceImpl Unit Tests (13 scenarios)
 */
@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    @Mock private DeliveryRepository deliveryRepository;
    @Mock private InvoiceRepository invoiceRepository;
    @Mock private PricingService pricingService;
    @Mock private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private DeliveryRequestDTO buildRequestDTO(String paymentMethod) {
        DeliveryRequestDTO dto = new DeliveryRequestDTO();
        dto.setSenderName("Alice Sender");
        DeliveryRequestDTO.AddressDTO senderAddr = new DeliveryRequestDTO.AddressDTO();
        senderAddr.setStreet("1 Sender St"); senderAddr.setCity("Mumbai"); senderAddr.setState("MH"); senderAddr.setZipCode("400001");
        dto.setSenderAddress(senderAddr);

        dto.setReceiverName("Bob Receiver");
        dto.setReceiverPhone("9876543210");
        DeliveryRequestDTO.AddressDTO receiverAddr = new DeliveryRequestDTO.AddressDTO();
        receiverAddr.setStreet("2 Receiver St"); receiverAddr.setCity("Delhi"); receiverAddr.setState("DL"); receiverAddr.setZipCode("110001");
        dto.setReceiverAddress(receiverAddr);

        DeliveryRequestDTO.PackageDTO packageDTO = new DeliveryRequestDTO.PackageDTO();
        packageDTO.setWeight(2.5);
        packageDTO.setDescription("Books");
        dto.setPackageDetails(packageDTO);

        dto.setPaymentMethod(paymentMethod);
        return dto;
    }

    private Delivery buildMockDelivery(String paymentMethod, DeliveryStatus status) {
        return Delivery.builder()
                .customerId(100L)
                .customerEmail("customer@test.com")
                .trackingNumber("TRK12345678")
                .senderName("Alice Sender")
                .receiverName("Bob Receiver")
                .receiverAddress(Address.builder().city("Delhi").build())
                .senderAddress(Address.builder().city("Mumbai").build())
                .packageDetails(PackageDetails.builder().weight(2.5).description("Books").build())
                .price(99.0)
                .paymentMethod(PaymentMethod.valueOf(paymentMethod))
                .paymentStatus("PAY_NOW".equals(paymentMethod) ? PaymentStatus.PAID : PaymentStatus.UNPAID)
                .status(status)
                .createdAt(Instant.now())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CREATE DELIVERY (Scenarios 1, 2, 3, 4, 5)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenarios 1, 3, 4: createDelivery(PAY_NOW) sets PAID, starts with 'TRK', publishes Booked & Update events")
    void createDelivery_PayNow_HappyPath() {
        DeliveryRequestDTO request = buildRequestDTO("PAY_NOW");
        when(pricingService.calculatePrice(2.5)).thenReturn(99.0);
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Delivery result = deliveryService.createDelivery(request, 100L, "customer@test.com");

        // Asserts
        assertTrue(result.getTrackingNumber().startsWith("TRK"));
        assertEquals(PaymentMethod.PAY_NOW, result.getPaymentMethod());
        assertEquals(PaymentStatus.PAID, result.getPaymentStatus());

        // Invoice capture to check paidAt
        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertNotNull(invoiceCaptor.getValue().getPaidAt());

        // Update event published
        verify(rabbitTemplate).convertAndSend(
                eq("delivery.exchange"), eq("delivery.routing.key"), any(DeliveryUpdateEvent.class));

        // Booked event published
        ArgumentCaptor<DeliveryBookedEvent> bookedCaptor = ArgumentCaptor.forClass(DeliveryBookedEvent.class);
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_BOOKED_ROUTING_KEY),
                bookedCaptor.capture());
        assertEquals("customer@test.com", bookedCaptor.getValue().getCustomerEmail());
        assertEquals(99.0, bookedCaptor.getValue().getPrice());
    }

    @Test
    @DisplayName("Scenario 2: createDelivery(PAY_ON_DELIVERY) sets UNPAID, null paidAt")
    void createDelivery_PayOnDelivery_HappyPath() {
        DeliveryRequestDTO request = buildRequestDTO("PAY_ON_DELIVERY");
        when(pricingService.calculatePrice(2.5)).thenReturn(99.0);
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Delivery result = deliveryService.createDelivery(request, 100L, "customer@test.com");

        assertEquals(PaymentStatus.UNPAID, result.getPaymentStatus());

        ArgumentCaptor<Invoice> invoiceCaptor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(invoiceCaptor.capture());
        assertNull(invoiceCaptor.getValue().getPaidAt());
    }

    @Test
    @DisplayName("Scenario 5: createDelivery() with null customerEmail skips sending Booked Event")
    void createDelivery_WhenEmailNull_SkipsBookedEvent() {
        DeliveryRequestDTO request = buildRequestDTO("PAY_NOW");
        when(pricingService.calculatePrice(2.5)).thenReturn(99.0);
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.createDelivery(request, 100L, null);

        // Update event still sent
        verify(rabbitTemplate).convertAndSend(
                eq("delivery.exchange"), eq("delivery.routing.key"), any(DeliveryUpdateEvent.class));

        // Booked event skipped
        verify(rabbitTemplate, never()).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_BOOKED_ROUTING_KEY),
                any(DeliveryBookedEvent.class));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UPDATE STATUS & MARK DELIVERED (Scenarios 6, 7, 8)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 6: updateDeliveryStatus(DELIVERED) on PAY_ON_DELIVERY updates invoice to PAID")
    void updateDeliveryStatus_ToDelivered_ShouldMarkInvoicePaid() {
        Delivery delivery = buildMockDelivery("PAY_ON_DELIVERY", DeliveryStatus.IN_TRANSIT);
        Invoice invoice = Invoice.builder().delivery(delivery).paymentStatus(PaymentStatus.UNPAID).build();

        when(deliveryRepository.findByTrackingNumber("TRK12345678")).thenReturn(Optional.of(delivery));
        when(invoiceRepository.findByDeliveryTrackingNumber("TRK12345678")).thenReturn(Optional.of(invoice));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.updateDeliveryStatus("TRK12345678", DeliveryStatus.DELIVERED, null, "Left at door");

        assertEquals(PaymentStatus.PAID, delivery.getPaymentStatus());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        assertNotNull(invoice.getPaidAt());
        verify(invoiceRepository).save(invoice);

        // Verify Delivered event published
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_DELIVERED_ROUTING_KEY),
                any(DeliveryDeliveredEvent.class));
    }

    @Test
    @DisplayName("Scenario 7: updateDeliveryStatus(IN_TRANSIT) does NOT update invoice or send Delivered event")
    void updateDeliveryStatus_ToInTransit_ShouldNotChangeInvoice() {
        Delivery delivery = buildMockDelivery("PAY_ON_DELIVERY", DeliveryStatus.BOOKED);
        when(deliveryRepository.findByTrackingNumber("TRK12345678")).thenReturn(Optional.of(delivery));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.updateDeliveryStatus("TRK12345678", DeliveryStatus.IN_TRANSIT, null, null);

        assertEquals(PaymentStatus.UNPAID, delivery.getPaymentStatus());
        verify(invoiceRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_DELIVERED_ROUTING_KEY),
                any(DeliveryDeliveredEvent.class));
    }

    @Test
    @DisplayName("Scenario 8: markAsDelivered() sets DELIVERED, updates PAY_ON_DELIVERY invoice, publishes event")
    void markAsDelivered_HappyPath() {
        Delivery delivery = buildMockDelivery("PAY_ON_DELIVERY", DeliveryStatus.OUT_FOR_DELIVERY);
        Invoice invoice = Invoice.builder().delivery(delivery).paymentStatus(PaymentStatus.UNPAID).build();

        when(deliveryRepository.findByTrackingNumber("TRK12345678")).thenReturn(Optional.of(delivery));
        when(invoiceRepository.findByDeliveryTrackingNumber("TRK12345678")).thenReturn(Optional.of(invoice));
        when(deliveryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        deliveryService.markAsDelivered("TRK12345678", "path/to/proof.jpg", "Signed by Bob");

        assertEquals(DeliveryStatus.DELIVERED, delivery.getStatus());
        assertEquals(PaymentStatus.PAID, invoice.getPaymentStatus());
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.NOTIFICATION_EXCHANGE),
                eq(RabbitMQConfig.DELIVERY_DELIVERED_ROUTING_KEY),
                any(DeliveryDeliveredEvent.class));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // GETTERS & SEARCH (Scenarios 9, 10, 11, 12, 13)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 9: getDeliveryByTrackingNumber() returns delivery")
    void getDeliveryByTrackingNumber_HappyPath() {
        Delivery delivery = buildMockDelivery("PAY_NOW", DeliveryStatus.BOOKED);
        when(deliveryRepository.findByTrackingNumber("TRK1")).thenReturn(Optional.of(delivery));

        Delivery result = deliveryService.getDeliveryByTrackingNumber("TRK1");
        assertNotNull(result);
    }

    @Test
    @DisplayName("Scenario 10: getDeliveryByTrackingNumber() throws when not found")
    void getDeliveryByTrackingNumber_WhenNotFound_ThrowsException() {
        when(deliveryRepository.findByTrackingNumber("TRK1")).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> deliveryService.getDeliveryByTrackingNumber("TRK1"));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    @DisplayName("Scenario 11: searchDeliveries() filters by status correctly")
    void searchDeliveries_WithStatus_ShouldFilter() {
        Delivery d1 = buildMockDelivery("PAY_NOW", DeliveryStatus.IN_TRANSIT);
        Delivery d2 = buildMockDelivery("PAY_NOW", DeliveryStatus.DELIVERED);
        when(deliveryRepository.findAll()).thenReturn(List.of(d1, d2));

        List<Delivery> result = deliveryService.searchDeliveries(DeliveryStatus.IN_TRANSIT, null, null, null);

        assertEquals(1, result.size());
        assertEquals(DeliveryStatus.IN_TRANSIT, result.get(0).getStatus());
    }

    @Test
    @DisplayName("Scenario 12: searchDeliveries() with all nulls returns all")
    void searchDeliveries_WithNulls_ShouldReturnAll() {
        Delivery d1 = buildMockDelivery("PAY_NOW", DeliveryStatus.IN_TRANSIT);
        Delivery d2 = buildMockDelivery("PAY_NOW", DeliveryStatus.DELIVERED);
        when(deliveryRepository.findAll()).thenReturn(List.of(d1, d2));

        List<Delivery> result = deliveryService.searchDeliveries(null, null, null, null);
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Scenario 13: getInvoiceByTrackingNumber() returns existing invoice")
    void getInvoiceByTrackingNumber_HappyPath() {
        Delivery delivery = buildMockDelivery("PAY_NOW", DeliveryStatus.BOOKED);
        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-123")
                .delivery(delivery)
                .amount(99.0)
                .weightKg(2.5)
                .paymentMethod(PaymentMethod.PAY_NOW)
                .paymentStatus(PaymentStatus.PAID)
                .createdAt(Instant.now())
                .paidAt(Instant.now())
                .build();

        when(invoiceRepository.findByDeliveryTrackingNumber("TRK12345678"))
                .thenReturn(Optional.of(invoice));

        InvoiceResponseDTO dto = deliveryService.getInvoiceByTrackingNumber("TRK12345678");

        assertEquals("INV-123", dto.getInvoiceNumber());
        assertEquals("TRK12345678", dto.getTrackingNumber());
        assertEquals(99.0, dto.getAmount());
    }
}