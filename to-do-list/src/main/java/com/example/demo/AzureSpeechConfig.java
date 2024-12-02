package com.example.demo;

import com.microsoft.cognitiveservices.speech.SpeechConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureSpeechConfig {

    @Value("${azure.speech.key}")
    private String speechKey;

    @Value("${azure.speech.region}")
    private String region;

    @Bean
    public SpeechConfig speechConfig() {
        if (speechKey == null || region == null) {
            throw new IllegalArgumentException("Chave ou região do Azure Speech não configurada corretamente.");
        }
        return SpeechConfig.fromSubscription(speechKey, region);
    }
}
