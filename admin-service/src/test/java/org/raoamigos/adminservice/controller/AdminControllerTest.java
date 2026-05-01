package org.raoamigos.adminservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.adminservice.client.DeliveryClient;
import org.raoamigos.adminservice.client.TrackingClient;
import org.raoamigos.adminservice.dto.ApiResponse;
import org.raoamigos.adminservice.dto.DeliveryDTO;
import org.raoamigos.adminservice.dto.TrackingEventDTO;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeliveryClient deliveryClient;

    @Mock
    private TrackingClient trackingClient;

    @InjectMocks
    private AdminController adminController;

    private DeliveryDTO dummyDelivery;
    private TrackingEventDTO dummyEvent;
    private final String TRACKING_NUMBER = "TRK12345678";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();

        dummyDelivery = new DeliveryDTO();
        dummyDelivery.setTrackingNumber(TRACKING_NUMBER);
        dummyDelivery.setStatus("IN_TRANSIT");

        dummyEvent = new TrackingEventDTO();
        dummyEvent.setTrackingNumber(TRACKING_NUMBER);
        dummyEvent.setStatus("IN_TRANSIT");
    }

    @Test
    void fetchDeliveryFromOtherService_ShouldReturnDataFromDeliveryClient() throws Exception {
        ApiResponse<DeliveryDTO> mockResponse = ApiResponse.success("Fetched", dummyDelivery);
        when(deliveryClient.getDeliveryByTrackingNumber(TRACKING_NUMBER)).thenReturn(mockResponse);

        mockMvc.perform(get("/admin/deliveries/{trackingNumber}", TRACKING_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Successfully fetched cross-service data"))
                .andExpect(jsonPath("$.data.trackingNumber").value(TRACKING_NUMBER))
                .andExpect(jsonPath("$.data.status").value("IN_TRANSIT"));
    }

    @Test
    void resolveDeliveryException_ShouldCallDeliveryClientAndReturnUpdatedData() throws Exception {
        dummyDelivery.setStatus("RESOLVED");
        ApiResponse<DeliveryDTO> mockResponse = ApiResponse.success("Updated", dummyDelivery);

        when(deliveryClient.updateDeliveryStatus(eq(TRACKING_NUMBER), eq("RESOLVED"), any(), any())).thenReturn(mockResponse);

        mockMvc.perform(put("/admin/deliveries/{trackingNumber}/resolve", TRACKING_NUMBER)
                        .param("newStatus", "RESOLVED")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Exception resolved successfully"))
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }

    @Test
    void getDashboardStats_ShouldAggregateDataFromBothClients() throws Exception {
        when(deliveryClient.getTotalDeliveries()).thenReturn(ApiResponse.success("Counted", 150L));
        when(trackingClient.getTotalTrackingEvents()).thenReturn(ApiResponse.success("Counted", 450L));

        mockMvc.perform(get("/admin/dashboard/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Dashboard stats aggregated successfully"))
                .andExpect(jsonPath("$.data.totalActiveDeliveries").value(150))
                .andExpect(jsonPath("$.data.totalTrackingEvents").value(450));
    }

    @Test
    void getMasterDashboardSummary_ShouldOrchestrateMultipleFeignCalls() throws Exception {
        when(deliveryClient.getTotalDeliveries()).thenReturn(ApiResponse.success("Total", 500L));
        when(deliveryClient.countDeliveriesByStatus("IN_TRANSIT")).thenReturn(ApiResponse.success("In Transit", 50L));
        when(deliveryClient.countDeliveriesByStatus("DELIVERED")).thenReturn(ApiResponse.success("Delivered", 450L));
        when(trackingClient.getRecentSystemEvents(anyInt())).thenReturn(List.of(dummyEvent, dummyEvent));

        mockMvc.perform(get("/admin/dashboard/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Master Dashboard Summary fetched"))
                .andExpect(jsonPath("$.data.totalDeliveries").value(500))
                .andExpect(jsonPath("$.data.activeInTransit").value(50))
                .andExpect(jsonPath("$.data.totalDelivered").value(450))
                .andExpect(jsonPath("$.data.recentEvents").value(2));
    }
}