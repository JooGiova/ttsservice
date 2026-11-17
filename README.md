# 🎙️ TTS Service API (Spring Boot + Piper TTS)

Servizio backend REST sviluppato in **Java e Spring Boot** che integra il motore neurale open-source **Piper TTS** in locale (self-hosted) per convertire testo e documenti (`.txt`, `.docx`) in file audio `.wav`.

Il progetto nasce come modulo centrale ("core backend") progettato per servire diversi client esterni, tra cui un bot Telegram e un'applicazione mobile Android/Flutter, senza dipendere da API vocali a pagamento.

---

## ✨ Funzionalità principali

* **Sintesi vocale da JSON (`POST /api/tts`):** Converte stringhe di testo in audio con selezione dinamica del modello vocale.
* **Sintesi vocale da file (`POST /api/tts/file`):** Estrae automaticamente il testo da file `.txt` (UTF-8) e `.docx` (tramite Apache POI) e genera l'audio corrispondente.
* **Esecuzione locale e privata:** Si interfaccia direttamente con Piper TTS su ambiente Linux tramite `ProcessBuilder`, passando il testo sullo Standard Input per evitare rischi di Command Injection.
* **Gestione sicura delle risorse:** Generazione di file temporanei univoci (`UUID`) con pulizia automatica garantita su disco al termine di ogni richiesta.
* **Gestione centralizzata degli errori:** Risposte JSON strutturate (`ErrorResponse`) tramite `@RestControllerAdvice` per errori di validazione, file vuoti o formati non supportati.
* **Configurazione multi-ambiente:** Supporto ai profili Spring (`application-linux.properties`) per separare i percorsi di sviluppo da quelli del server di esecuzione.

---

## 🛠️ Stack Tecnologico

* **Linguaggio:** Java 21
* **Framework:** Spring Boot 4 (Spring Web, Bean Validation)
* **Elaborazione Documenti:** Apache POI (`poi-ooxml`)
* **Motore TTS:** Piper TTS (modelli neurali `.onnx` su Linux)
* **Build Tool:** Maven (con Maven Wrapper incluso)

---

## 📂 Struttura del Progetto

```text
src/main/java/com/joogiova/ttsservice
├── config          # Mappatura delle proprietà di configurazione (TtsProperties)
├── controller      # Endpoint REST HTTP (TtsController)
├── dto             # Record per richieste e risposte di errore (TtsRequest, ErrorResponse)
├── exception       # Gestore globale delle eccezioni (GlobalExceptionHandler)
└── service         # Logica di generazione audio ed estrazione testo (TtsService, TextExtractorService)
```

---

## 🚀 Installazione e Avvio (Linux)

### 1. Prerequisiti
* **Java JDK 21** (o superiore)
* **Python 3** con `piper-tts` installato in un ambiente virtuale e almeno un modello vocale (`.onnx` + `.onnx.json`) scaricato.

### 2. Configurazione
Modifica il file `src/main/resources/application-linux.properties` inserendo i percorsi assoluti della tua macchina:

```properties
tts.piper.executable=/percorso/assoluto/venv/bin/piper
tts.piper.model-dir=/percorso/assoluto/models
tts.piper.default-voice=it_IT-paola-medium.onnx
tts.temp-dir=/tmp/tts-audio
```

### 3. Avvio dell'applicazione
Clona il repository e avvia Spring Boot attivando il profilo `linux`:

```bash
git clone https://github.com/JooGiova/ttsservice
cd tts-service
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=linux
```

Il server sarà in ascolto su `http://localhost:8080`.

---

## 📡 Esempi di utilizzo (API)

### 1. Generare audio da testo JSON
* **Endpoint:** `POST /api/tts`
* **Content-Type:** `application/json`

```bash
curl -X POST http://localhost:8080/api/tts \
  -H "Content-Type: application/json" \
  -d '{"text": "Ciao, questo audio è stato generato in locale con Spring Boot e Piper!", "voice": "it_IT-paola-medium.onnx"}' \
  --output output.wav
```

### 2. Generare audio da file `.txt` o `.docx`
* **Endpoint:** `POST /api/tts/file`
* **Content-Type:** `multipart/form-data`

```bash
curl -X POST http://localhost:8080/api/tts/file \
  -F "file=@documento.docx" \
  --output output_file.wav
```

---

## 🔮 Sviluppi Futuri

* [ ] Collegamento con **Bot Telegram** per la lettura diretta di documenti inviati in chat.
* [ ] Sviluppo di un client mobile in **Flutter** per la riproduzione e il salvataggio locale degli audio.
* [ ] Supporto all'estrazione di testo da file `.epub` e `.pdf`.
