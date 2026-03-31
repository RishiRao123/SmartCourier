package org.raoamigos.trackingservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.repository.DocumentRepository;
import org.raoamigos.trackingservice.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepository documnetRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public Document uploadDocument(String trackingNumber, MultipartFile file) {

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
                    .build();

            log.info("USER DIR: {}", System.getProperty("user.dir"));
            log.info("UPLOAD PATH: {}", uploadPath.toAbsolutePath());
            log.info("Successfully uploaded file {} for tracking {}", uniqueFileName, trackingNumber);
            return documnetRepository.save(document);

        } catch (IOException ex) {
            log.error("Failed to store file {}", originalFileName, ex);
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }


}
