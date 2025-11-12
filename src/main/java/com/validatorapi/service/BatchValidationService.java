package com.validatorapi.service;

import com.validatorapi.dto.BatchEmailValidationResponse;
import com.validatorapi.dto.EmailValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchValidationService {

    private final EmailValidatorService emailValidatorService;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    public BatchEmailValidationResponse validateBatch(List<String> emails) {
        long startTime = System.currentTimeMillis();

        log.info("Starting batch validation for {} emails", emails.size());

        List<CompletableFuture<EmailValidationResponse>> futures = emails.stream()
                .map(email -> CompletableFuture.supplyAsync(
                        () -> emailValidatorService.validate(email),
                        executorService
                ))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<EmailValidationResponse> validationResults = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        long validCount = validationResults.stream()
                .filter(EmailValidationResponse::getValid)
                .count();

        List<Map<String, Object>> simplifiedResults = validationResults.stream()
                .map(this::simplifyResult)
                .toList();

        long processingTime = System.currentTimeMillis() - startTime;

        log.info("Batch completed: {} valid, {} invalid, {}ms",
                validCount, emails.size() - validCount, processingTime);

        return BatchEmailValidationResponse.builder()
                .total(emails.size())
                .valid((int) validCount)
                .invalid((int) (emails.size() - validCount))
                .processingTimeMs(processingTime)
                .checkedAt(LocalDateTime.now())
                .results(simplifiedResults)
                .build();
    }

    private Map<String, Object> simplifyResult(EmailValidationResponse response) {
        Map<String, Object> simplified = new HashMap<>();
        simplified.put("email", response.getEmail());
        simplified.put("valid", response.getValid());
        simplified.put("is_disposable", response.getIsDisposable());
        simplified.put("risk_score", response.getRiskScore());
        return simplified;
    }
}