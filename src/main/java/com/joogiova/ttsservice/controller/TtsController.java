package com.joogiova.ttsservice.controller;

import com.joogiova.ttsservice.dto.TtsRequest;
import com.joogiova.ttsservice.service.TtsService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tts")
public class TtsController {

    private final TtsService ttsService;

    // Constructor Injection: Spring ci passa l'istanza di TtsService
    public TtsController(TtsService ttsService) {
        this.ttsService = ttsService;
    }

    @PostMapping
    public ResponseEntity<byte[]> generateSpeech(@Valid @RequestBody TtsRequest request) {
        // 1. Chiamiamo il service per ottenere i byte del file .wav
        byte[] audioBytes = ttsService.generateAudio(request);

        // 2. Prepariamo gli header HTTP per dire al client che gli stiamo inviando un file audio
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("audio/wav"));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename("speech.wav").build()
        );

        // 3. Restituiamo status 200 OK, gli header e i byte dell'audio
        return ResponseEntity.ok()
                .headers(headers)
                .body(audioBytes);
    }
}
