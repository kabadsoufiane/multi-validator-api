package com.validatorapi.service;

import com.validatorapi.dto.ComboValidationResponse;
import com.validatorapi.dto.EmailValidationResponse;
import com.validatorapi.dto.PhoneValidationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComboValidationService {

    private final EmailValidatorService emailValidatorService;
    private final PhoneValidatorService phoneValidatorService;

    public ComboValidationResponse validateCombo(String email, String phone, String country) {
        long startTime = System.currentTimeMillis();

        log.info("Combo validation: {} | {}", email, phone);

        CompletableFuture<EmailValidationResponse> emailFuture =
                CompletableFuture.supplyAsync(() -> emailValidatorService.validate(email));

        CompletableFuture<PhoneValidationResponse> phoneFuture =
                CompletableFuture.supplyAsync(() -> phoneValidatorService.validate(phone, country));

        CompletableFuture.allOf(emailFuture, phoneFuture).join();

        EmailValidationResponse emailResponse = emailFuture.join();
        PhoneValidationResponse phoneResponse = phoneFuture.join();

        int overallRiskScore = (int) (
                (emailResponse.getRiskScore() != null ? emailResponse.getRiskScore() : 0) * 0.6 +
                        (phoneResponse.getRiskScore() != null ? phoneResponse.getRiskScore() : 0) * 0.4
        );

        long validationTime = System.currentTimeMillis() - startTime;

        return ComboValidationResponse.builder()
                .emailValidation(emailResponse)
                .phoneValidation(phoneResponse)
                .overallRiskScore(overallRiskScore)
                .validationTimeMs(validationTime)
                .checkedAt(LocalDateTime.now())
                .build();
    }
}