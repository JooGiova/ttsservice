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
        // 1. Scegliamo la voce (quella richiesta oppure quella di default)
        String voiceFile = (request.voice() != null && !request.voice().isBlank())
                ? request.voice()
                : ttsProperties.getPiper().getDefaultVoice();

        // Sicurezza base: evitiamo che qualcuno passi "../" nel nome della voce
        String safeVoiceName = Paths.get(voiceFile).getFileName().toString();
        Path modelPath = Paths.get(ttsProperties.getPiper().getModelDir(), safeVoiceName);

        // 2. Prepariamo la cartella temporanea e un nome file univoco (es. 550e8400-e29b...wav)
        Path tempDir = Paths.get(ttsProperties.getTempDir());
        Path outputFile = tempDir.resolve(UUID.randomUUID() + ".wav");

        try {
            // Crea la cartella temp se non esiste ancora
            Files.createDirectories(tempDir);

            // 3. Costruiamo il comando da lanciare su Linux:
            // piper --model /percorso/voce.onnx --output_file /tmp/tts-audio/uuid.wav
            ProcessBuilder processBuilder = new ProcessBuilder(
                    ttsProperties.getPiper().getExecutable(),
                    "--model", modelPath.toAbsolutePath().toString(),
                    "--output_file", outputFile.toAbsolutePath().toString()
            );

            // Unisce eventuali messaggi di errore di Piper nello stesso stream di output
            processBuilder.redirectErrorStream(true);

            // 4. Avviamo il processo e gli "scriviamo" il testo nello Standard Input (come farebbe echo "..." | piper)
            Process process = processBuilder.start();
            try (OutputStream os = process.getOutputStream()) {
                os.write(request.text().getBytes(StandardCharsets.UTF_8));
                os.flush();
            } // La chiusura automatica del try-with-resources dice a Piper che il testo è finito

            // 5. Aspettiamo che Piper finisca (senza limiti di tempo)
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                String errorOutput = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                throw new RuntimeException("Errore durante l'esecuzione di Piper: " + errorOutput);
            }

            // 6. Leggiamo tutti i byte del file .wav appena creato e li restituiamo
            return Files.readAllBytes(outputFile);

        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new RuntimeException("Errore tecnico nella generazione dell'audio", e);
        } finally {
            // 7. PULIZIA: Questo blocco viene eseguito SEMPRE (anche in caso di errore)
            // Cancella il file temporaneo dal disco per non riempire mai il server
            try {
                Files.deleteIfExists(outputFile);
            } catch (IOException ignored) {
            }
        }
    }
}
