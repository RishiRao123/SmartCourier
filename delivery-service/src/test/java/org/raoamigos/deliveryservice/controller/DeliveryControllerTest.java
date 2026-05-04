package org.raoamigos.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.dto.InvoiceResponseDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Phase 4 — DeliveryController @WebMvcTest (7 scenarios)
 *
 * Uses the real SecurityConfig which permits all at the filter level but parses headers.
 * Needs mocked beans for HeaderAuthenticationFilter (if it depends on anything).
 */
@WebMvcTest(DeliveryController.class)
@Import({org.raoamigos.deliveryservice.security.SecurityConfig.class, org.raoamigos.deliveryservice.security.HeaderAuthenticationFilter.class})
class DeliveryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private DeliveryService deliveryService;
    @MockBean private DeliveryRepository deliveryRepository;

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private DeliveryRequestDTO buildRequestDTO() {
        DeliveryRequestDTO dto = new DeliveryRequestDTO();
        dto.setSenderName("Alice");
        DeliveryRequestDTO.AddressDTO senderAddr = new DeliveryRequestDTO.AddressDTO();
        senderAddr.setStreet("1 St"); senderAddr.setCity("Mumbai"); senderAddr.setState("MH"); senderAddr.setZipCode("400001");
        dto.setSenderAddress(senderAddr);

        dto.setReceiverName("Bob");
        dto.setReceiverPhone("9876543210");
        DeliveryRequestDTO.AddressDTO receiverAddr = new DeliveryRequestDTO.AddressDTO();
        receiverAddr.setStreet("2 St"); receiverAddr.setCity("Delhi"); receiverAddr.setState("DL"); receiverAddr.setZipCode("110001");
        dto.setReceiverAddress(receiverAddr);

        DeliveryRequestDTO.PackageDTO packageDTO = new DeliveryRequestDTO.PackageDTO();
        packageDTO.setWeight(2.5);
        packageDTO.setDescription("Books");
        dto.setPackageDetails(packageDTO);

        dto.setPaymentMethod("PAY_NOW");
        return dto;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Scenarios
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Scenario 1: POST /deliveries returns 200 and json with trackingNumber")
    void createDelivery_HappyPath_Returns200() throws Exception {
        DeliveryRequestDTO dto = buildRequestDTO();
        Delivery delivery = Delivery.builder().trackingNumber("TRK123").build();

        when(deliveryService.createDelivery(any(), eq(100L), eq("test@test.com"))).thenReturn(delivery);

        mockMvc.perform(post("/deliveries")
                        .header("X-User-Id", "100")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK123"));
    }

    @Test
    @DisplayName("Scenario 2: POST /deliveries returns 400 when body fails validation")
    void createDelivery_ValidationFail_Returns400() throws Exception {
        DeliveryRequestDTO dto = new DeliveryRequestDTO(); // Empty, fails @NotNull

        mockMvc.perform(post("/deliveries")
                        .header("X-User-Id", "100")
                        .header("X-User-Email", "test@test.com")
                        .header("X-User-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Scenario 3: GET /deliveries/{trackingNumber} serializes createdAt as ISO-8601 UTC")
    void getDelivery_ReturnsIso8601Utc() throws Exception {
        Instant now = Instant.parse("2026-05-04T14:00:00Z");
        Delivery delivery = Delivery.builder().trackingNumber("TRK123").createdAt(now).build();

        when(deliveryService.getDeliveryByTrackingNumber("TRK123")).thenReturn(delivery);

        mockMvc.perform(get("/deliveries/TRK123")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createdAt").value("2026-05-04T14:00:00Z"));
    }

    @Test
    @DisplayName("Scenario 4: GET /deliveries/{trackingNumber} throws 400 BAD_REQUEST when service throws RuntimeException")
    void getDelivery_WhenNotFound_ThrowsException() throws Exception {
        when(deliveryService.getDeliveryByTrackingNumber("TRK999"))
                .thenThrow(new RuntimeException("Delivery not found"));

        mockMvc.perform(get("/deliveries/TRK999")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Scenario 5: GET /deliveries/{trackingNumber}/invoice returns InvoiceResponseDTO with UTC Instants")
    void getInvoice_ReturnsInvoiceResponseDTO() throws Exception {
        Instant now = Instant.parse("2026-05-04T14:00:00Z");
        InvoiceResponseDTO invoice = InvoiceResponseDTO.builder()
                .invoiceNumber("INV-001")
                .createdAt(now)
                .paidAt(now)
                .build();

        when(deliveryService.getInvoiceByTrackingNumber("TRK123")).thenReturn(invoice);

        mockMvc.perform(get("/deliveries/TRK123/invoice")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.invoiceNumber").value("INV-001"))
                .andExpect(jsonPath("$.data.createdAt").value("2026-05-04T14:00:00Z"))
                .andExpect(jsonPath("$.data.paidAt").value("2026-05-04T14:00:00Z"));
    }

    @Test
    @DisplayName("Scenario 6: PUT /deliveries/{trackingNumber}/status returns 200")
    void updateStatus_HappyPath_Returns200() throws Exception {
        Delivery delivery = Delivery.builder().trackingNumber("TRK123").status(DeliveryStatus.IN_TRANSIT).build();

        when(deliveryService.updateDeliveryStatus(eq("TRK123"), eq(DeliveryStatus.IN_TRANSIT), any(), any()))
                .thenReturn(delivery);

        mockMvc.perform(put("/deliveries/TRK123/status")
                        .param("status", "IN_TRANSIT")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));
    }

    @Test
    @DisplayName("Scenario 7: GET /deliveries/my scopes by X-User-Id header")
    void getMyDeliveries_UsesHeader_ReturnsList() throws Exception {
        Delivery d1 = Delivery.builder().trackingNumber("TRK1").build();
        when(deliveryService.getMyDeliveries(100L)).thenReturn(List.of(d1));

        mockMvc.perform(get("/deliveries/my")
                        .header("X-User-Id", "100")
                        .header("X-User-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].trackingNumber").value("TRK1"));
    }

}