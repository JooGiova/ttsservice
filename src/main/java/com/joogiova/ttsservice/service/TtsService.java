package com.joogiova.ttsservice.service;

import com.joogiova.ttsservice.config.TtsProperties;
import com.joogiova.ttsservice.dto.TtsRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class TtsService {

    private final TtsProperties ttsProperties;

    // Constructor Injection: Spring ci passa automaticamente TtsProperties già popolato
    public TtsService(TtsProperties ttsProperties) {
        this.ttsProperties = ttsProperties;
    }

    public byte[] generateAudio(TtsRequest request) {
        String voiceFile = (request.voice() != null && !request.voice().isBlank())
                ? request.voice()
                : ttsProperties.getPiper().getDefaultVoice();

        String safeVoiceName = Paths.get(voiceFile).getFileName().toString();
        Path modelPath = Paths.get(ttsProperties.getPiper().getModelDir(), safeVoiceName);

        // Prepariamo la cartella temporanea e un nome file univoco
        Path tempDir = Paths.get(ttsProperties.getTempDir());
        Path outputFile = tempDir.resolve(UUID.randomUUID() + ".wav");

        try {
            // Crea la cartella temp se non esiste ancora
            Files.createDirectories(tempDir);

            // piper --model /percorso/voce.onnx --output_file /tmp/tts-audio/uuid.wav
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ttsProperties.getPiper().getExecutable(),
                    "--model", modelPath.toAbsolutePath().toString(),
                    "--output_file", outputFile.toAbsolutePath().toString()
            );

            // Unisce eventuali messaggi di errore di Piper nello stesso stream di output
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            try (OutputStream os = process.getOutputStream()) {
                os.write(request.text().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errorOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Errore durante l'esecuzione di Piper: " + errorOutput);
            }

            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Errore tecnico nella generazione dell'audio", e);
        } finally {
            // PULIZIA: Questo blocco viene eseguito SEMPRE (anche in caso di errore)
            // Cancella il file temporaneo dal disco per non riempire mai il server
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {
            }
        }
    }
}
