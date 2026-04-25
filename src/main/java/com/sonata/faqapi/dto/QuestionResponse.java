package com.sonata.faqapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "FAQ answer response payload")
public class QuestionResponse {

   

	@Schema(description = "The original question asked")
    private String question;

    @Schema(description = "The AI-generated answer")
    private String answer;

    @Schema(description = "Whether the response was served from cache")
    private boolean cached;

    @Schema(description = "Duration of the AI call in milliseconds (null if cached)")
    private Long latencyMs;

    @Schema(description = "Timestamp of the response")
    private Instant timestamp;

    @Schema(description = "Unique request ID for tracing")
    private String requestId;

	public QuestionResponse(String question, String answer, boolean cached, Long latencyMs, Instant timestamp,
			String requestId) {
		super();
		this.question = question;
		this.answer = answer;
		this.cached = cached;
		this.latencyMs = latencyMs;
		this.timestamp = timestamp;
		this.requestId = requestId;
	}
    
}
