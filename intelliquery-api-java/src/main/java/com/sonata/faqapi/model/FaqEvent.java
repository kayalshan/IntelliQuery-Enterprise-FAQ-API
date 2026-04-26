package com.sonata.faqapi.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record FaqEvent(
        String requestId,
        EventType eventType,
        String question,
        String answer,
        boolean cached,
        Long latencyMs,
        String userAgent,
        String clientIp,
        Instant occurredAt,
        String errorMessage
) {
	public enum EventType {
        QUESTION_ASKED,
        ANSWER_DELIVERED,
        CACHE_HIT,
        AI_ERROR,
        RATE_LIMITED
    }
}