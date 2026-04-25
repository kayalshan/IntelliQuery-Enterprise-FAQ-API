package com.sonata.faqapi.service;

import java.time.Instant;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import com.sonata.faqapi.config.CacheConfig;
import com.sonata.faqapi.controller.FaqController;
import com.sonata.faqapi.dto.QuestionRequest;
import com.sonata.faqapi.dto.QuestionResponse;
import com.sonata.faqapi.kafka.FaqEventPublisher;
import com.sonata.faqapi.model.FaqEvent;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FaqService {

	private final OpenAiService openAiService;
	private final FaqEventPublisher eventPublisher;
	private final CacheManager cacheManager;
	private final Counter cacheHitCounter;
	private final Counter cacheMissCounter;
	private static final Logger log = LoggerFactory.getLogger(FaqService.class);

	public FaqService(OpenAiService openAiService, FaqEventPublisher eventPublisher, CacheManager cacheManager,
			MeterRegistry meterRegistry) {
		this.openAiService = openAiService;
		this.eventPublisher = eventPublisher;
		this.cacheManager = cacheManager;
		this.cacheHitCounter = Counter.builder("faq.cache.hits").description("Number of FAQ cache hits")
				.register(meterRegistry);
		this.cacheMissCounter = Counter.builder("faq.cache.misses").description("Number of FAQ cache misses")
				.register(meterRegistry);
	}

	public QuestionResponse processQuestion(QuestionRequest request) {
		String requestId = UUID.randomUUID().toString();
		String normalizedQuestion = request.getQuestion().toLowerCase().trim();

		log.info("[requestId={}] Processing question: '{}'", requestId, normalizedQuestion);

		boolean isCached = isCacheHit(normalizedQuestion);

		long start = System.currentTimeMillis();
		String answer = openAiService.getAnswer(request.getQuestion());
		long latencyMs = System.currentTimeMillis() - start;

		if (isCached) {
			cacheHitCounter.increment();
			log.debug("[requestId={}] Cache HIT for question", requestId);
		} else {
			cacheMissCounter.increment();
			log.debug("[requestId={}] Cache MISS — AI call took {}ms", requestId, latencyMs);
		}

		publishEvent(requestId, request, answer, isCached, isCached ? null : latencyMs);

//        return QuestionResponse.builder()
//                .question(request.getQuestion())
//                .answer(answer)
//                .cached(isCached)
//                .latencyMs(isCached ? null : latencyMs)
//                .timestamp(Instant.now())
//                .requestId(requestId)
//                .build();
		return new QuestionResponse(
		        request.getQuestion(),
		        answer,
		        isCached,
		        isCached ? null : latencyMs,
		        Instant.now(),
		        requestId
		);
	}

	private boolean isCacheHit(String normalizedQuestion) {
		try {
			Cache cache = cacheManager.getCache(CacheConfig.FAQ_CACHE);
			if (cache == null)
				return false;
			return cache.get(normalizedQuestion) != null;
		} catch (Exception e) {
			log.warn("Could not check cache status: {}", e.getMessage());
			return false;
		}
	}

	private void publishEvent(String requestId, QuestionRequest request, String answer, boolean cached,
			Long latencyMs) {
//		FaqEvent event = FaqEvent.builder().requestId(requestId)
//				.eventType(cached ? FaqEvent.EventType.CACHE_HIT : FaqEvent.EventType.ANSWER_DELIVERED)
//				.question(request.getQuestion()).answer(answer).cached(cached).latencyMs(latencyMs)
//				.occurredAt(Instant.now()).build();
		
		FaqEvent event = new FaqEvent(
		        requestId,
		        cached ? FaqEvent.EventType.CACHE_HIT : FaqEvent.EventType.ANSWER_DELIVERED,
		        request.getQuestion(),
		        answer,
		        cached,
		        latencyMs,
		        null, // userAgent
		        null, // clientIp
		        Instant.now(),
		        null  // errorMessage
		);

		eventPublisher.publish(event);
	}
}
