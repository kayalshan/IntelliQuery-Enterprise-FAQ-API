package com.sonata.faqapi.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sonata.faqapi.dto.ErrorResponse;
import com.sonata.faqapi.dto.QuestionRequest;
import com.sonata.faqapi.dto.QuestionResponse;
import com.sonata.faqapi.service.FaqService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/faq")
@RequiredArgsConstructor
@Tag(name = "FAQ", description = "FAQ endpoints powered by GPT-4")
@SecurityRequirement(name = "ApiKey")
public class FaqController {

    FaqService faqService;
    

	private static final Logger log = LoggerFactory.getLogger(FaqController.class);
    @Operation(
            summary = "Ask an FAQ question",
            description = "Submit a question and receive an AI-generated answer. "
                    + "Responses are cached in Redis for 1 hour to reduce latency and API costs."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Answer retrieved successfully",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = QuestionResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "question": "What is SuperWidget?",
                                      "answer": "SuperWidget is a versatile productivity tool...",
                                      "cached": false,
                                      "latencyMs": 842,
                                      "timestamp": "2025-01-15T10:30:00Z",
                                      "requestId": "550e8400-e29b-41d4-a716-446655440000"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
            @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "502", description = "OpenAI service error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Service temporarily unavailable (circuit open)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(value = "/ask",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<QuestionResponse> ask(@Valid @RequestBody QuestionRequest request) {
        log.info("Received FAQ question: '{}'", request.getQuestion());
        QuestionResponse response = faqService.processQuestion(request);
        return ResponseEntity.ok(response);
    }
}
