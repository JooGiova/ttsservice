package com.joogiova.ttsservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TtsRequest(

        @NotBlank(message = "Il testo da leggere non può essere vuoto")
        @Size(max = 10000, message = "Il testo non può superare i 10000 caratteri")
        String text,

        String voice
) {
}