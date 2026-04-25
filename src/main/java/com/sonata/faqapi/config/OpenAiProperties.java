package com.sonata.faqapi.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@Validated
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

    @NotBlank
    private String apiKey;

    private String apiUrl = "https://api.openai.com/v1/chat/completions";

    private String model = "gpt-4";

    @Positive
    private int maxTokens = 300;

    private double temperature = 0.7;

    private String systemPrompt = "You are a helpful FAQ assistant for a product called SuperWidget. "
            + "Provide concise, accurate, and helpful answers. "
            + "If you don't know the answer, say so clearly.";

    @Positive
    private int connectTimeoutSeconds = 5;

    @Positive
    private int readTimeoutSeconds = 30;

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getMaxTokens() {
		return maxTokens;
	}

	public void setMaxTokens(int maxTokens) {
		this.maxTokens = maxTokens;
	}

	public double getTemperature() {
		return temperature;
	}

	public void setTemperature(double temperature) {
		this.temperature = temperature;
	}

	public String getSystemPrompt() {
		return systemPrompt;
	}

	public void setSystemPrompt(String systemPrompt) {
		this.systemPrompt = systemPrompt;
	}

	public int getConnectTimeoutSeconds() {
		return connectTimeoutSeconds;
	}

	public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
		this.connectTimeoutSeconds = connectTimeoutSeconds;
	}

	public int getReadTimeoutSeconds() {
		return readTimeoutSeconds;
	}

	public void setReadTimeoutSeconds(int readTimeoutSeconds) {
		this.readTimeoutSeconds = readTimeoutSeconds;
	}


}
