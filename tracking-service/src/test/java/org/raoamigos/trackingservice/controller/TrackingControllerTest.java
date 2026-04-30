package org.raoamigos.trackingservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.raoamigos.trackingservice.service.DocumentService;
import org.raoamigos.trackingservice.service.TrackingService;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrackingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrackingService trackingService;

    @Mock
    private DocumentService documentService;

    @Mock
    private TrackingEventRepository trackingEventRepository;

    @InjectMocks
    private TrackingController trackingController;

    private TrackingEvent dummyEvent;
    private final String TRACKING_NUMBER = "TRK12345678";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(trackingController).build();

        dummyEvent = TrackingEvent.builder()
                .trackingNumber(TRACKING_NUMBER)
                .status("IN_TRANSIT")
                .message("Arrived at sorting facility")
                .build();
    }

    @Test
    void getTrackingHistory_ShouldReturnApiResponseWithList() throws Exception {
        when(trackingService.getTrackingHistory(TRACKING_NUMBER)).thenReturn(List.of(dummyEvent));

        mockMvc.perform(get("/tracking/{trackingNumber}", TRACKING_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Tracking history fetched successfully"))
                .andExpect(jsonPath("$.data[0].status").value("IN_TRANSIT"));
    }

    @Test
    void uploadDocument_ShouldReturnApiResponseWithDocument() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "receipt.pdf",
                "application/pdf",
                "Dummy PDF Data".getBytes()
        );

        Document dummyDocument = Document.builder()
                .trackingNumber(TRACKING_NUMBER)
                .fileName("receipt.pdf")
                .build();

        when(documentService.uploadDocument(eq(TRACKING_NUMBER), any(), eq(1L), eq("ADMIN"))).thenReturn(dummyDocument);

        mockMvc.perform(multipart("/tracking/{trackingNumber}/documents", TRACKING_NUMBER)
                        .file(mockFile)
                        .header("X-User-Id", 1L)
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Document uploaded successfully"))
                .andExpect(jsonPath("$.data.fileName").value("receipt.pdf"));
    }

    @Test
    void getTotalTrackingEvents_ShouldReturnRepositoryCount() throws Exception {
        when(trackingEventRepository.count()).thenReturn(500L);

        mockMvc.perform(get("/tracking/admin/stats/count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Total tracking events fetched"))
                .andExpect(jsonPath("$.data").value(500));
    }

    @Test
    void getLatestStatus_ShouldReturnEventDirectly() throws Exception {
        when(trackingService.getLatestEvent(TRACKING_NUMBER)).thenReturn(dummyEvent);

        mockMvc.perform(get("/tracking/{trackingNumber}/latest", TRACKING_NUMBER)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_TRANSIT"))
                .andExpect(jsonPath("$.message").value("Arrived at sorting facility"));
    }
}