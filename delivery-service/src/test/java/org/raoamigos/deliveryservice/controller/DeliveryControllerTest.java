package org.raoamigos.deliveryservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.deliveryservice.dto.DeliveryRequestDTO;
import org.raoamigos.deliveryservice.entity.Delivery;
import org.raoamigos.deliveryservice.entity.DeliveryStatus;
import org.raoamigos.deliveryservice.repository.DeliveryRepository;
import org.raoamigos.deliveryservice.service.DeliveryService;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class DeliveryControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private DeliveryService deliveryService;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryController deliveryController;

    private Delivery dummyDelivery;
    private DeliveryRequestDTO dummyRequestDTO;
    private final Long CUSTOMER_ID = 101L;
    private final String CUSTOMER_EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(deliveryController).build();

        dummyRequestDTO = new DeliveryRequestDTO();
        dummyRequestDTO.setSenderName("John Doe");
        dummyRequestDTO.setReceiverName("Jane Doe");

        DeliveryRequestDTO.AddressDTO address = new DeliveryRequestDTO.AddressDTO();
        address.setStreet("123 Main St");
        address.setCity("Metropolis");
        address.setState("NY");
        address.setZipCode("10001");

        dummyRequestDTO.setSenderAddress(address);
        dummyRequestDTO.setReceiverAddress(address);

        DeliveryRequestDTO.PackageDTO pkg = new DeliveryRequestDTO.PackageDTO();
        pkg.setWeight(2.5);
        pkg.setDescription("Books");
        dummyRequestDTO.setPackageDetails(pkg);
        dummyRequestDTO.setPaymentMethod("PAY_ON_DELIVERY");

        dummyDelivery = Delivery.builder()
                .customerId(CUSTOMER_ID)
                .trackingNumber("TRK12345678")
                .status(DeliveryStatus.BOOKED)
                .build();
    }

    @Test
    void createDelivery_ShouldReturn200AndDeliveryData() throws Exception {
        when(deliveryService.createDelivery(any(DeliveryRequestDTO.class), eq(CUSTOMER_ID), eq(CUSTOMER_EMAIL)))
                .thenReturn(dummyDelivery);

        mockMvc.perform(post("/deliveries")
                        .header("X-User-Id", CUSTOMER_ID)
                        .header("X-User-Email", CUSTOMER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delivery created successfully"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK12345678"))
                .andExpect(jsonPath("$.data.status").value("BOOKED"));
    }

    @Test
    void getDelivery_ShouldReturn200AndDeliveryData() throws Exception {
        when(deliveryService.getDeliveryByTrackingNumber("TRK12345678"))
                .thenReturn(dummyDelivery);

        mockMvc.perform(get("/deliveries/{trackingNumber}", "TRK12345678")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Delivery fetched successfully"))
                .andExpect(jsonPath("$.data.trackingNumber").value("TRK12345678"));
    }

    @Test
    void getMyDeliveries_ShouldReturn200AndList() throws Exception {
        when(deliveryService.getMyDeliveries(CUSTOMER_ID))
                .thenReturn(List.of(dummyDelivery));

        mockMvc.perform(get("/deliveries/my")
                        .header("X-User-Id", CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User deliveries fetched successfully"))
                .andExpect(jsonPath("$.data[0].trackingNumber").value("TRK12345678"));
    }

    @Test
    void updateStatus_ShouldReturn200AndUpdatedDelivery() throws Exception {
        dummyDelivery.setStatus(DeliveryStatus.IN_TRANSIT);

        when(deliveryService.updateDeliveryStatus(eq("TRK12345678"), eq(DeliveryStatus.IN_TRANSIT), any(), any()))
                .thenReturn(dummyDelivery);

        mockMvc.perform(put("/deliveries/{trackingNumber}/status", "TRK12345678")
                        .param("status", "IN_TRANSIT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));
    }

    @Test
    void createDelivery_WhenPayloadIsInvalid_ShouldReturn400BadRequest() throws Exception {
        DeliveryRequestDTO badRequestDTO = new DeliveryRequestDTO();

        mockMvc.perform(post("/deliveries")
                        .header("X-User-Id", CUSTOMER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badRequestDTO)))
                .andExpect(status().isBadRequest());

        verify(deliveryService, times(0)).createDelivery(any(), any(), any());
    }
}