package com.sonata.faqapi.exception;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sonata.faqapi.dto.ErrorResponse;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        BindingResult result = ex.getBindingResult();
//        List<ErrorResponse.FieldError> fieldErrors = result.getFieldErrors().stream()
//                .map(fe -> ErrorResponse.FieldError.builder()
//                        .field(fe.getField())
//                        .message(fe.getDefaultMessage())
//                        .rejectedValue(fe.getRejectedValue())
//                        .build())
//                .toList();
        List<ErrorResponse.FieldError> fieldErrors = result.getFieldErrors().stream()
                .map(fe -> new ErrorResponse.FieldError(
                        fe.getField(),
                        fe.getDefaultMessage(),
                        fe.getRejectedValue()
                ))
                .toList();
//        ErrorResponse error = ErrorResponse.builder()
//                .status(HttpStatus.BAD_REQUEST.value())
//                .error("Validation Failed")
//                .message("One or more fields are invalid")
//                .path(request.getRequestURI())
//                .timestamp(Instant.now())
//                .fieldErrors(fieldErrors)
//                .requestId(UUID.randomUUID().toString())
//                .build();

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "One or more fields are invalid",
                request.getRequestURI(),
                Instant.now(),
                fieldErrors,
                UUID.randomUUID().toString()
        );

        log.warn("Validation error on {}: {}", request.getRequestURI(), fieldErrors);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(OpenAiException.class)
    public ResponseEntity<ErrorResponse> handleOpenAi(
            OpenAiException ex, HttpServletRequest request) {

        log.error("OpenAI error: {}", ex.getMessage(), ex);
        ErrorResponse error = buildError(
                HttpStatus.BAD_GATEWAY, "OpenAI Service Error", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(error);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(
            RateLimitExceededException ex, HttpServletRequest request) {

        log.warn("Rate limit exceeded: {}", ex.getMessage());
        ErrorResponse error = buildError(
                HttpStatus.TOO_MANY_REQUESTS, "Rate Limit Exceeded", ex.getMessage(), request);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(error);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    public ResponseEntity<ErrorResponse> handleCircuitBreaker(
            CallNotPermittedException ex, HttpServletRequest request) {

        log.warn("Circuit breaker open: {}", ex.getMessage());
        ErrorResponse error = buildError(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Service Temporarily Unavailable",
                "The AI service is currently unavailable. Please try again later.",
                request);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest request) {

        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        ErrorResponse error = buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred. Please try again.",
                request);
        return ResponseEntity.internalServerError().body(error);
    }

    private ErrorResponse buildError(HttpStatus status, String error,
                                     String message, HttpServletRequest request) {
        return new ErrorResponse(
        		 status.value(),
        	        error,
        	        message,
        	        request.getRequestURI(),
        	        Instant.now(),
        	        null, // fieldErrors
        	        UUID.randomUUID().toString());
    }
}
