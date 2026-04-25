package com.sonata.faqapi.kafka;

import com.sonata.faqapi.config.KafkaConfig;
import com.sonata.faqapi.health.OpenAiHealthIndicator;
import com.sonata.faqapi.model.FaqEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaqEventPublisher {
	private static final Logger log = LoggerFactory.getLogger(FaqEventPublisher.class);
    private final KafkaTemplate<String, FaqEvent> kafkaTemplate = null;

    @Async
    public void publish(FaqEvent event) {
        CompletableFuture<SendResult<String, FaqEvent>> future =
                kafkaTemplate.send(KafkaConfig.FAQ_EVENTS_TOPIC, event.requestId(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish FAQ event [requestId={}]: {}",
                        event.requestId(), ex.getMessage(), ex);
            } else {
                log.debug("Published FAQ event [requestId={}, offset={}, eventType={}]",
                        event.requestId(),
                        result.getRecordMetadata().offset(),
                        event.eventType());
            }
        });
    }
}
