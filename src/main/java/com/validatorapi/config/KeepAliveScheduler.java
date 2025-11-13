package com.validatorapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.keep-alive.enabled", havingValue = "true")
public class KeepAliveScheduler {
    @Value("${app.keep-alive.url}")
    private String keepAliveUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Scheduled(fixedRate = 600000)
    public void keepAlive() {
        try {
            restTemplate.getForObject(keepAliveUrl, String.class);
            log.info("⏰ Keep-alive ping successful");
        } catch (Exception e) {
            log.warn("⚠️ Keep-alive ping failed: {}", e.getMessage());
        }
    }
}