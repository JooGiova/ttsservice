package com.joogiova.ttsservice.service;

import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
public class TextExtractorService {

    public String extractText(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Il file caricato è vuoto o mancante.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Nome del file non valido.");
        }

        String lowerName = originalFilename.toLowerCase();

        try (InputStream inputStream = file.getInputStream()) {
            if (lowerName.endsWith(".txt")) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            } else if (lowerName.endsWith(".docx")) {
                try (XWPFDocument document = new XWPFDocument(inputStream);
                     XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                    return extractor.getText();
                }
            } else {
                throw new IllegalArgumentException("Formato non supportato. Carica un file .txt o .docx");
            }
        } catch (IOException e) {
            throw new RuntimeException("Errore durante la lettura del file", e);
        }
    }
}
