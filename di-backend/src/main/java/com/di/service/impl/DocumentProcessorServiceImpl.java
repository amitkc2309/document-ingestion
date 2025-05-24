package com.di.service.impl;

import com.di.dto.DocumentProcessingMessage;
import com.di.model.Document;
import com.di.model.ProcessingStatus;
import com.di.model.elasticsearch.DocumentIndex;
import com.di.repository.DocumentRepository;
import com.di.repository.elasticsearch.DocumentIndexRepository;
import com.di.service.DocumentProcessorService;
import com.di.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.Iterator;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessorServiceImpl implements DocumentProcessorService {

    private final DocumentRepository documentRepository;
    private final DocumentIndexRepository documentIndexRepository;
    private final FileStorageService fileStorageService;

    /**
     * Kafka listener for document processing messages.
     *
     * @param message The document processing message
     */
    @KafkaListener(topics = "${application.kafka.topics.document-upload}", groupId = "${spring.kafka.consumer.group-id}")
    @Override
    @Transactional
    public void processDocument(DocumentProcessingMessage message) {
        log.info("Processing document: {}", message.getDocumentId());
        
        try {
            // Get the document from the database
            Document document = documentRepository.findById(message.getDocumentId())
                    .orElseThrow(() -> new RuntimeException("Document not found: " + message.getDocumentId()));
            
            // Update document status to PROCESSING
            document.setProcessingStatus(ProcessingStatus.PROCESSING);
            documentRepository.save(document);
            
            // Extract text from the document
            String textContent = extractTextFromFile(document.getFilePath());
            
            // Update document with extracted text and status
            document.setProcessingStatus(ProcessingStatus.COMPLETED);
            document.setProcessedDate(LocalDateTime.now());
            documentRepository.save(document);
            
            // Index the document in Elasticsearch
            DocumentIndex documentIndex = DocumentIndex.builder()
                    .id(document.getId().toString())
                    .title(document.getTitle())
                    .author(document.getAuthor())
                    .content(textContent)
                    .fileName(document.getFileName())
                    .documentType(document.getDocumentType())
                    .uploadedBy(document.getUploadedBy().getUsername())
                    .databaseId(document.getId())
                    .build();
            
            documentIndexRepository.save(documentIndex);
            
            // Update document with Elasticsearch ID
            document.setElasticsearchId(documentIndex.getId());
            documentRepository.save(document);
            
            log.info("Document processed successfully: {}", document.getId());
        } catch (Exception e) {
            log.error("Error processing document: {}", message.getDocumentId(), e);
            
            try {
                // Update document status to FAILED
                Document document = documentRepository.findById(message.getDocumentId()).orElse(null);
                if (document != null) {
                    document.setProcessingStatus(ProcessingStatus.FAILED);
                    document.setProcessingError("Error processing document: " + e.getMessage());
                    documentRepository.save(document);
                }
            } catch (Exception ex) {
                log.error("Error updating document status: {}", message.getDocumentId(), ex);
            }
        }
    }
    
    /**
     * Extract text from a file.
     *
     * @param filePath The path to the file
     * @return The extracted text
     * @throws IOException If an I/O error occurs
     */
    private String extractTextFromFile(String filePath) throws IOException {
        if (filePath == null) {
            return "";
        }
        
        String fileName = filePath.toLowerCase();
        
        try (InputStream inputStream = fileStorageService.getFile(filePath)) {
            if (fileName.endsWith(".pdf")) {
                return extractTextFromPdf(inputStream);
            } else if (fileName.endsWith(".docx")) {
                return extractTextFromDocx(inputStream);
            } else if (fileName.endsWith(".xlsx")) {
                return extractTextFromXlsx(inputStream);
            } else if (fileName.endsWith(".txt")) {
                return extractTextFromTxt(inputStream);
            } else {
                return "";
            }
        }
    }
    
    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }
    
    private String extractTextFromDocx(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            XWPFWordExtractor extractor = new XWPFWordExtractor(document);
            return extractor.getText();
        }
    }
    
    private String extractTextFromXlsx(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheetAt(i);
                Iterator<Row> rowIterator = sheet.iterator();
                while (rowIterator.hasNext()) {
                    Row row = rowIterator.next();
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        text.append(cell.toString()).append(" ");
                    }
                    text.append("\n");
                }
            }
        }
        return text.toString();
    }
    
    private String extractTextFromTxt(InputStream inputStream) throws IOException {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                text.append(line).append("\n");
            }
        }
        return text.toString();
    }
}