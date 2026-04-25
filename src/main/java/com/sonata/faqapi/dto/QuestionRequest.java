package com.sonata.faqapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "FAQ question request payload")
public class QuestionRequest {

    @NotBlank(message = "Question must not be blank")
    @Size(min = 3, max = 500, message = "Question must be between 3 and 500 characters")
    @Schema(description = "The FAQ question to ask", example = "What is SuperWidget?")
    private String question;

    @Schema(description = "Optional context for more targeted answers", example = "billing")
    private String context;

	public QuestionRequest(String string) {
		// TODO Auto-generated constructor stub
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}
    
    
}
