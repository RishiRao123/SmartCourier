package org.raoamigos.trackingservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.trackingservice.dto.ApiResponse;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.raoamigos.trackingservice.service.DocumentService;
import org.raoamigos.trackingservice.service.TrackingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;


@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;
    private final DocumentService documentService;
    private final TrackingEventRepository trackingEventRepository;

    @GetMapping("/{trackingNumber}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')") // Both can view history
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getTrackingHistory(@PathVariable String trackingNumber) {
        List<TrackingEvent> history = trackingService.getTrackingHistory(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Tracking history fetched successfully", history));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only Admins can view ALL trackings everywhere
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getAllTrackings() {
        List<TrackingEvent> trackings = trackingService.getAllTrackings();
        return ResponseEntity.ok(ApiResponse.success("Trackings fetched successfully", trackings));
    }

    @PostMapping(value = "/{trackingNumber}/documents", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('ADMIN')") // Only internal staff/admins upload proof documents
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @PathVariable String trackingNumber,
            @RequestParam("file") MultipartFile file) {
        Document uploadedDoc = documentService.uploadDocument(trackingNumber, file);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", uploadedDoc));
    }

    @GetMapping("/stats/count")
    @PreAuthorize("hasRole('ADMIN')") // Only Admins can see stats
    public ResponseEntity<ApiResponse<Long>> getTotalTrackingEvents() {
        long count = trackingEventRepository.count();
        return ResponseEntity.ok(ApiResponse.success("Total tracking events fetched", count));
    }
}