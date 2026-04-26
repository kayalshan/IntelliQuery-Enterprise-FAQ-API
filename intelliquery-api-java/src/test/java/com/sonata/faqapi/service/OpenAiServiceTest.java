package com.sonata.faqapi.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sonata.faqapi.config.OpenAiProperties;

@ExtendWith(MockitoExtension.class)
class OpenAiServiceTest {

    @Mock
    private OpenAiProperties props;

    @Test
    void fallbackAnswer_returnsServiceUnavailableMessage() {
        OpenAiService service = new OpenAiService(new OpenAiProperties());
        String fallback = service.fallbackAnswer("test question", new RuntimeException("timeout"));
        assertThat(fallback).contains("technical difficulties");
    }

    @Test
    void openAiProperties_defaultValues() {
        OpenAiProperties p = new OpenAiProperties();
        assertThat(p.getModel()).isEqualTo("gpt-4");
        assertThat(p.getMaxTokens()).isEqualTo(300);
        assertThat(p.getTemperature()).isEqualTo(0.7);
        assertThat(p.getConnectTimeoutSeconds()).isEqualTo(5);
        assertThat(p.getReadTimeoutSeconds()).isEqualTo(30);
    }
}
