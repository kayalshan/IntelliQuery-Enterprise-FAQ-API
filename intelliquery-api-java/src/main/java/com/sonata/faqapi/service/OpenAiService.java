package com.sonata.faqapi.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sonata.faqapi.config.CacheConfig;
import com.sonata.faqapi.config.OpenAiProperties;
import com.sonata.faqapi.exception.OpenAiException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OpenAiService {

    private final OpenAiProperties props;
    private final ObjectMapper objectMapper;
    private final CloseableHttpClient httpClient;
	private static final Logger log = LoggerFactory.getLogger(OpenAiService.class);

    public OpenAiService(OpenAiProperties props) {
        this.props = props;
        this.objectMapper = new ObjectMapper();

        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(props.getConnectTimeoutSeconds()))
                .setResponseTimeout(Timeout.ofSeconds(props.getReadTimeoutSeconds()))
                .build();

        this.httpClient = HttpClients.custom()
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    /**
     * Fetches an AI-generated answer for a given FAQ question.
     * Responses are cached in Redis with a configurable TTL to reduce API costs.
     */
    @Cacheable(
            value = CacheConfig.FAQ_CACHE,
            key = "#question.toLowerCase().trim()",
            unless = "#result == null"
    )
    @CircuitBreaker(name = "openai", fallbackMethod = "fallbackAnswer")
    @Retry(name = "openai")
    @Timed(value = "faq.openai.request", description = "Time taken to call OpenAI API")
    public String getAnswer(String question) {
        log.info("Calling OpenAI API for question: '{}'", abbreviate(question, 80));
        long start = System.currentTimeMillis();

        try {
            HttpPost post = buildRequest(question);

            ClassicHttpResponse response = httpClient.execute(post);
            int statusCode = response.getCode();

            if (statusCode != 200) {
                String body = new String(response.getEntity().getContent().readAllBytes(),
                        StandardCharsets.UTF_8);
                throw new OpenAiException("OpenAI API returned HTTP " + statusCode + ": " + body);
            }

            InputStream content = response.getEntity().getContent();
            JsonNode root = objectMapper.readTree(content);
            JsonNode choices = root.get("choices");

            if (choices == null || !choices.isArray() || choices.isEmpty()) {
                throw new OpenAiException("OpenAI returned empty choices");
            }

            String answer = choices.get(0).get("message").get("content").asText().trim();
            long latency = System.currentTimeMillis() - start;

            log.info("OpenAI responded in {}ms for question: '{}'", latency, abbreviate(question, 60));
            return answer;

        } catch (OpenAiException e) {
            throw e;
        } catch (Exception e) {
            throw new OpenAiException("Failed to communicate with OpenAI API: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback when the circuit breaker is open or retries are exhausted.
     */
    public String fallbackAnswer(String question, Throwable t) {
        log.warn("OpenAI circuit breaker fallback triggered for question '{}': {}",
                abbreviate(question, 60), t.getMessage());
        return "We're currently experiencing technical difficulties reaching our AI service. "
                + "Please try again later or contact support.";
    }

    private HttpPost buildRequest(String question) throws Exception {
        HttpPost post = new HttpPost(props.getApiUrl());
        post.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey());
        post.addHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        ObjectNode body = objectMapper.createObjectNode();
        body.put("model", props.getModel());
        body.put("max_tokens", props.getMaxTokens());
        body.put("temperature", props.getTemperature());

        ArrayNode messages = body.putArray("messages");

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", props.getSystemPrompt());

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", question);

        post.setEntity(new StringEntity(objectMapper.writeValueAsString(body),
                StandardCharsets.UTF_8));
        return post;
    }

    private String abbreviate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
