package com.sonata.faqapi.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.sonata.faqapi.config.CacheConfig;
import com.sonata.faqapi.dto.QuestionRequest;
import com.sonata.faqapi.dto.QuestionResponse;
import com.sonata.faqapi.kafka.FaqEventPublisher;
import com.sonata.faqapi.model.FaqEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AgentClientService {

    private final RestClient agentClient;

    public AgentClientService(RestClient.Builder builder) {
        // Points to the Python module running in a separate container/port
        this.agentClient = builder.baseUrl("http://intelliquery-agent-python:8000").build();
    }

    @CircuitBreaker(name = "agenticBrain", fallbackMethod = "getStaticFaq")
    public FaqResponse processWithAgents(String userQuery) {
        return agentClient.post()
            .uri("/v1/agents/process")
            .body(new AgentRequest(userQuery))
            .retrieve()
            .body(FaqResponse.class);
    }

    public FaqResponse getStaticFaq(String query, Exception e) {
        // Fallback to basic keyword search or static message if Agent is offline
        return new FaqResponse("The AI agent is currently busy. Please try again soon.");
    }
}