package com.sonata.faqapi.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import com.sonata.faqapi.config.OpenAiProperties;

@Slf4j
@Component("openai")
@RequiredArgsConstructor
public class OpenAiHealthIndicator implements HealthIndicator {
	private static final Logger log = LoggerFactory.getLogger(OpenAiHealthIndicator.class);
	private final OpenAiProperties props = new OpenAiProperties();

    @Override
    public Health health() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet get = new HttpGet("https://api.openai.com/v1/models");
            get.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getApiKey());

            ClassicHttpResponse response = client.execute(get);
            int code = response.getCode();

            if (code == 200 || code == 401) {
                // 401 means key issue, but OpenAI is reachable
                return Health.up()
                        .withDetail("openai.reachable", true)
                        .withDetail("openai.model", props.getModel())
                        .build();
            } else {
                return Health.down()
                        .withDetail("openai.reachable", false)
                        .withDetail("http.status", code)
                        .build();
            }
        } catch (Exception e) {
            log.warn("OpenAI health check failed: {}", e.getMessage());
            return Health.down()
                    .withDetail("openai.reachable", false)
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
