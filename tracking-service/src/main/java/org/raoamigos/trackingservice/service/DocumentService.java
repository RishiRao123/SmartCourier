package org.raoamigos.trackingservice.service;

import org.raoamigos.trackingservice.entity.Document;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {

    public Document uploadDocument(String trackingNumber, MultipartFile file);
}
