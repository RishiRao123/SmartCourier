package org.raoamigos.trackingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.repository.DocumentRepository;
import org.raoamigos.trackingservice.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documnetRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public Document uploadDocument(String trackingNumber, MultipartFile file, Long userId, String role) {

        if(file.isEmpty()) {
            throw new RuntimeException("Cannot upload an empty file");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFileName = UUID.randomUUID().toString().substring(0, 8) + "_" + originalFileName;

        try {
            Path uploadPath = Paths.get(uploadDir);
            if(!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Document document = Document.builder()
                    .trackingNumber(trackingNumber)
                    .fileName(uniqueFileName)
                    .fileType(file.getContentType())
                    .filePath(filePath.toString())
                    .uploadedBy(userId)
                    .uploaderRole(role)
                    .build();

            log.info("Successfully uploaded file {} for tracking {} by {} ({})", uniqueFileName, trackingNumber, userId, role);
            return documnetRepository.save(document);

        } catch (IOException ex) {
            log.error("Failed to store file {}", originalFileName, ex);
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    @Override
    public List<Document> getDocumentsByTrackingNumber(String trackingNumber) {
        return documnetRepository.findByTrackingNumber(trackingNumber);
    }

    @Override
    public Resource getDocumentFile(Long documentId) {
        Document document = documnetRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
        
        try {
            Path path = Paths.get(document.getFilePath());
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public Document getDocumentMetadata(Long documentId) {
        return documnetRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + documentId));
    }
}
