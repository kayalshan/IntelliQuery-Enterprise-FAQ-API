//package com.sonata.faqapi.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.sonata.faqapi.dto.QuestionRequest;
//import com.sonata.faqapi.dto.QuestionResponse;
//import com.sonata.faqapi.service.FaqService;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.Instant;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@WebMvcTest(FaqController.class)
//class FaqControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private FaqService faqService;
//
//    @Test
//    @WithMockUser(roles = "API_USER")
//    void ask_validQuestion_returns200() throws Exception {
//
//        QuestionRequest request = new QuestionRequest(
//                "What is SuperWidget?"
//        );
//
//        QuestionResponse response = new QuestionResponse(
//                "What is SuperWidget?",
//                "SuperWidget is a versatile productivity tool.",
//                false,
//                500L,
//                Instant.now(),
//                "test-request-id"
//        );
//
//        when(faqService.processQuestion(any())).thenReturn(response);
//
//        mockMvc.perform(post("/api/v1/faq/ask")
//                        .with(csrf())
//                        .header("X-API-KEY", "test-key")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.question").value("What is SuperWidget?"))
//                .andExpect(jsonPath("$.answer").value("SuperWidget is a versatile productivity tool."))
//                .andExpect(jsonPath("$.cached").value(false))
//                .andExpect(jsonPath("$.requestId").value("test-request-id"));
//    }
//
//    @Test
//    @WithMockUser(roles = "API_USER")
//    void ask_blankQuestion_returns400() throws Exception {
//
//        QuestionRequest request = new QuestionRequest(
//                ""
//        );
//
//        mockMvc.perform(post("/api/v1/faq/ask")
//                        .with(csrf())
//                        .header("X-API-KEY", "test-key")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.error").value("Validation Failed"))
//                .andExpect(jsonPath("$.fieldErrors[0].field").value("question"));
//    }
//
//    @Test
//    @WithMockUser(roles = "API_USER")
//    void ask_questionTooLong_returns400() throws Exception {
//
//        QuestionRequest request = new QuestionRequest(
//                "a".repeat(501)
//        );
//
//        mockMvc.perform(post("/api/v1/faq/ask")
//                        .with(csrf())
//                        .header("X-API-KEY", "test-key")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    void ask_noApiKey_returns401() throws Exception {
//
//        QuestionRequest request = new QuestionRequest(
//                "What is SuperWidget?"
//        );
//
//        mockMvc.perform(post("/api/v1/faq/ask")
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isUnauthorized());
//    }
//}