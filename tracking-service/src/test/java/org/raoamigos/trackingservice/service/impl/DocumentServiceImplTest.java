package org.raoamigos.trackingservice.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raoamigos.trackingservice.entity.Document;
import org.raoamigos.trackingservice.repository.DocumentRepository;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(documentService, "uploadDir", tempDir.toString());
    }

    @Test
    void uploadDocument_ShouldSaveFileAndReturnDocument() {
        MockMultipartFile fakeFile = new MockMultipartFile(
                "file",
                "invoice.pdf",
                "application/pdf",
                "Dummy PDF Content".getBytes()
        );

        Document mockedSavedDoc = Document.builder().fileName("unique_invoice.pdf").build();
        when(documentRepository.save(any(Document.class))).thenReturn(mockedSavedDoc);

        Document result = documentService.uploadDocument("TRK123", fakeFile);

        assertNotNull(result);
        verify(documentRepository, times(1)).save(any(Document.class));
    }

    @Test
    void uploadDocument_ShouldThrowException_WhenFileIsEmpty() {
        MockMultipartFile emptyFile = new MockMultipartFile("file", new byte[0]);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            documentService.uploadDocument("TRK123", emptyFile);
        });

        assertEquals("Cannot upload an empty file", exception.getMessage());
        verify(documentRepository, never()).save(any());
    }
}