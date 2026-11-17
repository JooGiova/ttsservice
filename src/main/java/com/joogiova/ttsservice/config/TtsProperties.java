package com.joogiova.ttsservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "tts")
public class TtsProperties {

    private Piper piper = new Piper();
    private String tempDir;

    @Data
    public static class Piper {
        private String executable;
        private String modelDir;
        private String defaultVoice;
    }
}