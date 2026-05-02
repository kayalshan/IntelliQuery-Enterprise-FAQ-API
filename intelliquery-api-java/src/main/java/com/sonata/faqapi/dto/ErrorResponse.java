package com.sonata.faqapi.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Standard API error response")
public class ErrorResponse {

    @Schema(description = "HTTP status code")
    private int status;

    @Schema(description = "Error type")
    private String error;

    @Schema(description = "Human-readable error message")
    private String message;

    @Schema(description = "Request path that triggered the error")
    private String path;

    @Schema(description = "Timestamp of the error")
    private Instant timestamp;

    @Schema(description = "Field-level validation errors")
    private List<FieldError> fieldErrors;

    @Schema(description = "Unique request ID for tracing")
    private String requestId;






	public ErrorResponse(int status, String error, String message, String path, Instant timestamp,
			List<FieldError> fieldErrors, String requestId) {
		super();
		this.status = status;
		this.error = error;
		this.message = message;
		this.path = path;
		this.timestamp = timestamp;
		this.fieldErrors = fieldErrors;
		this.requestId = requestId;
	}



	public int getStatus() {
		return status;
	}



	public void setStatus(int status) {
		this.status = status;
	}



	public String getError() {
		return error;
	}



	public void setError(String error) {
		this.error = error;
	}



	public String getMessage() {
		return message;
	}



	public void setMessage(String message) {
		this.message = message;
	}



	public String getPath() {
		return path;
	}



	public void setPath(String path) {
		this.path = path;
	}



	public Instant getTimestamp() {
		return timestamp;
	}



	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}



	public List<FieldError> getFieldErrors() {
		return fieldErrors;
	}



	public void setFieldErrors(List<FieldError> fieldErrors) {
		this.fieldErrors = fieldErrors;
	}



	public String getRequestId() {
		return requestId;
	}



	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}



	@Data
    @Builder
    public static class FieldError {

		public FieldError(String field, String message, Object rejectedValue) {
			super();
			this.field = field;
			this.message = message;
			this.rejectedValue = rejectedValue;
		}
		private String field;
        private String message;
        private Object rejectedValue;
    }
}
