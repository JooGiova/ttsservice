package com.joogiova.ttsservice.controller;

import com.joogiova.ttsservice.dto.TtsRequest;
import com.joogiova.ttsservice.service.TextExtractorService;
import com.joogiova.ttsservice.service.TtsService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;
    private final TextExtractorService textExtractorService;

    public TtsController(TtsService ttsService, TextExtractorService textExtractorService) {
        this.ttsService = ttsService;
        this.textExtractorService = textExtractorService;
    }

    @PostMapping
    public ResponseEntity<byte[]> generateSpeech(@Valid @RequestBody TtsRequest request) {
        byte[] audioBytes = ttsService.generateAudio(request);
        return buildWavResponse(audioBytes);
    }

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<byte[]> generateSpeechFromFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "voice", required = false) String voice) {

        // Estrae il testo dal file (.txt o .docx)
        String extractedText = textExtractorService.extractText(file);

        TtsRequest request = new TtsRequest(extractedText, voice);
        byte[] audioBytes = ttsService.generateAudio(request);

        return buildWavResponse(audioBytes);
    }

    private ResponseEntity<byte[]> buildWavResponse(byte[] audioBytes) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("speech.wav").build()
        );

        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }
}
