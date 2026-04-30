package org.raoamigos.trackingservice.service;

import org.raoamigos.trackingservice.entity.Document;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.core.io.Resource;
import java.util.List;

public interface DocumentService {

    public Document uploadDocument(String trackingNumber, MultipartFile file, Long userId, String role);
    List<Document> getDocumentsByTrackingNumber(String trackingNumber);
    Resource getDocumentFile(Long documentId);
    Document getDocumentMetadata(Long documentId);
}
