package com.validatorapi.service;

import com.validatorapi.model.Plan;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimitService {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String apiKey, Plan plan) {
        return cache.computeIfAbsent(apiKey, k -> createBucket(plan));
    }

    private Bucket createBucket(Plan plan) {
        Bandwidth limit = Bandwidth.classic(
                plan.getRequestsPerMinute(),
                Refill.intervally(plan.getRequestsPerMinute(), Duration.ofMinutes(1))
        );

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    public boolean tryConsume(String apiKey, Plan plan) {
        Bucket bucket = resolveBucket(apiKey, plan);
        boolean consumed = bucket.tryConsume(1);

        if (!consumed) {
            log.warn("Rate limit exceeded for API key: {}", apiKey);
        }

        return consumed;
    }
}