package com.di.unit;

import com.di.dto.DocumentDTO;
import com.di.exception.ResourceNotFoundException;
import com.di.model.Document;
import com.di.model.DocumentType;
import com.di.model.ProcessingStatus;
import com.di.model.User;
import com.di.repository.DocumentRepository;
import com.di.repository.UserRepository;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.FileStorageService;
import com.di.service.impl.DocumentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private DocumentIndexRepository documentIndexRepository;

    @InjectMocks
    private DocumentServiceImpl documentService;

    private User testUser;
    private Document testDocument;
    private MockMultipartFile testFile;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testDocument = Document.builder()
                .id(1L)
                .title("Test Document")
                .fileName("test.pdf")
                .contentType("application/pdf")
                .fileSize(1024L)
                .author("Test Author")
                .uploadDate(LocalDateTime.now())
                .uploadedBy(testUser)
                .documentType(DocumentType.PDF)
                .processingStatus(ProcessingStatus.PENDING)
                .build();

        testFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
    }

    @Test
    void uploadDocument_Success() throws Exception {
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(testUser));
        when(documentRepository.save(any(Document.class))).thenReturn(testDocument);
        when(fileStorageService.storeFile(any(), any())).thenReturn("/path/to/file");

        DocumentDTO result = documentService.uploadDocument(
                testFile,
                "Test Document",
                "Test Author",
                "testuser"
        );

        assertNotNull(result);
        assertEquals("Test Document", result.getTitle());
        assertEquals("Test Author", result.getAuthor());
        assertEquals(DocumentType.PDF, result.getDocumentType());
        assertEquals("testuser", result.getUploadedBy());

        verify(documentRepository, times(2)).save(any(Document.class));
        verify(fileStorageService).storeFile(any(), any());
    }

    @Test
    void findByDocumentType_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Document> documentPage = new PageImpl<>(
                Collections.singletonList(testDocument),
                pageable,
                1
        );

        when(documentRepository.findByDocumentType(any(DocumentType.class), any(Pageable.class)))
                .thenReturn(documentPage);

        Page<DocumentDTO> result = documentService.findByDocumentType(DocumentType.PDF, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
        
        DocumentDTO firstDoc = result.getContent().get(0);
        assertEquals("Test Document", firstDoc.getTitle());
        assertEquals("Test Author", firstDoc.getAuthor());
        assertEquals(DocumentType.PDF, firstDoc.getDocumentType());
        assertEquals("testuser", firstDoc.getUploadedBy());

        verify(documentRepository).findByDocumentType(DocumentType.PDF, pageable);
    }
} 