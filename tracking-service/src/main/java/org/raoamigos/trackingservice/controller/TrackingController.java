package org.raoamigos.trackingservice.controller;

import lombok.RequiredArgsConstructor;
import org.raoamigos.trackingservice.dto.ApiResponse;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.entity.TrackingEvent;
import org.raoamigos.trackingservice.repository.TrackingEventRepository;
import org.raoamigos.trackingservice.service.DocumentService;
import org.raoamigos.trackingservice.service.TrackingService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getTrackingHistory(@PathVariable String trackingNumber) {
        List<TrackingEvent> history = trackingService.getTrackingHistory(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Tracking history fetched successfully", history));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<TrackingEvent>>> getAllTrackings() {
        List<TrackingEvent> trackings = trackingService.getAllTrackings();
        return ResponseEntity.ok(ApiResponse.success("Trackings fetched successfully", trackings));
    }

    @PostMapping(value = "/{trackingNumber}/documents", consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Document>> uploadDocument(
            @PathVariable String trackingNumber,
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Role") String role) {
        Document uploadedDoc = documentService.uploadDocument(trackingNumber, file, userId, role);
        return ResponseEntity.ok(ApiResponse.success("Document uploaded successfully", uploadedDoc));
    }

    @GetMapping("/{trackingNumber}/documents")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<Document>>> getDocuments(@PathVariable String trackingNumber) {
        List<Document> documents = documentService.getDocumentsByTrackingNumber(trackingNumber);
        return ResponseEntity.ok(ApiResponse.success("Documents fetched successfully", documents));
    }

    @GetMapping("/documents/{id}/download")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id) {
        Resource file = documentService.getDocumentFile(id);
        Document metadata = documentService.getDocumentMetadata(id);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + metadata.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .body(file);
    }

    @GetMapping("/documents/file/{id}")
    public ResponseEntity<Resource> serveDocument(@PathVariable Long id) {
        Resource file = documentService.getDocumentFile(id);
        Document metadata = documentService.getDocumentMetadata(id);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getFileType()))
                .body(file);
    }

    @GetMapping("admin/stats/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')") // Only Admins can see stats
    public ResponseEntity<ApiResponse<Long>> getTotalTrackingEvents() {
        long count = trackingEventRepository.count();
        return ResponseEntity.ok(ApiResponse.success("Total tracking events fetched", count));
    }

    @GetMapping("/{trackingNumber}/latest")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<TrackingEvent> getLatestStatus(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getLatestEvent(trackingNumber));
    }

    @GetMapping("/{trackingNumber}/count")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Long> getUpdateCount(@PathVariable String trackingNumber) {
        return ResponseEntity.ok(trackingService.getUpdateCount(trackingNumber));
    }

    @GetMapping("/admin/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TrackingEvent>> getEventsByStatus(@PathVariable String status) {
        return ResponseEntity.ok(trackingService.getEventsByStatus(status));
    }

    @GetMapping("/admin/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<List<TrackingEvent>> getRecentEvents(@RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(trackingService.getRecentSystemEvents(days));
    }
}