package com.validatorapi.service;

import com.validatorapi.dto.EmailValidationResponse;
import com.validatorapi.model.ValidationHistory;
import com.validatorapi.repository.DisposableDomainRepository;
import com.validatorapi.repository.ValidationHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.InitialDirContext;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailValidatorService {

    private final DisposableDomainRepository disposableDomainRepository;
    private final ValidationHistoryRepository validationHistoryRepository;

    private static final Set<String> FREE_PROVIDERS = Set.of(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "live.com", "aol.com", "icloud.com", "mail.com"
    );

    private static final Set<String> ROLE_ACCOUNTS = Set.of(
            "admin", "info", "support", "contact", "noreply",
            "no-reply", "postmaster", "webmaster", "sales",
            "marketing", "billing", "help", "service"
    );

    private static final Map<String, String> COMMON_TYPOS = Map.of(
            "gmai.com", "gmail.com",
            "gmial.com", "gmail.com",
            "gmil.com", "gmail.com",
            "yahooo.com", "yahoo.com",
            "yaho.com", "yahoo.com",
            "outlok.com", "outlook.com"
    );

    @Cacheable(value = "emailValidation", key = "#email", unless = "#result.riskScore < 50")
    public EmailValidationResponse validate(String email) {
        long startTime = System.currentTimeMillis();

        email = email.trim().toLowerCase();

        EmailValidationResponse.EmailValidationResponseBuilder builder =
                EmailValidationResponse.builder()
                        .email(email)
                        .checkedAt(LocalDateTime.now());

        // Étape 1 : Validation syntaxe
        boolean syntaxValid = validateSyntax(email);
        builder.syntaxValid(syntaxValid);

        if (!syntaxValid) {
            builder.valid(false)
                    .riskScore(0)
                    .validationTimeMs(System.currentTimeMillis() - startTime);
            return builder.build();
        }

        // Extraction domaine
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];

        // Étape 2 : Vérification DNS/MX
        MxCheckResult mxResult = checkMxRecords(domain);
        builder.domainExists(mxResult.exists)
                .mxHost(mxResult.mxHost)
                .mxRecordsCount(mxResult.count);

        // Étape 3 : Détection disposable
        boolean isDisposable = disposableDomainRepository.existsByDomain(domain);
        builder.isDisposable(isDisposable);

        // Étape 4 : Détection role account
        boolean isRoleAccount = isRoleAccount(localPart);
        builder.isRoleAccount(isRoleAccount);

        // Étape 5 : Détection provider type
        boolean isFreeProvider = FREE_PROVIDERS.contains(domain);
        builder.isFreeProvider(isFreeProvider);

        String providerType = determineProviderType(domain, isFreeProvider);
        builder.providerType(providerType);

        // Étape 6 : Suggestion typo
        String suggestion = suggestCorrection(email, domain);
        builder.suggestion(suggestion);

        // Étape 7 : Calcul risk score
        int riskScore = calculateRiskScore(
                syntaxValid, mxResult.exists, isDisposable,
                isRoleAccount, providerType
        );
        builder.riskScore(riskScore);

        // Validation finale
        boolean isValid = syntaxValid && mxResult.exists && !isDisposable;
        builder.valid(isValid);

        long validationTime = System.currentTimeMillis() - startTime;
        builder.validationTimeMs(validationTime);

        // Sauvegarde asynchrone en BDD
        saveHistory(email, isValid, riskScore, validationTime);

        log.info("Email validated: {} - valid: {} - score: {} - time: {}ms",
                email, isValid, riskScore, validationTime);

        return builder.build();
    }

    private boolean validateSyntax(String email) {
        EmailValidator validator = EmailValidator.getInstance(false);
        return validator.isValid(email);
    }

    private MxCheckResult checkMxRecords(String domain) {
        try {
            Attribute attr = new InitialDirContext()
                    .getAttributes("dns:/" + domain, new String[]{"MX"})
                    .get("MX");

            if (attr == null || attr.size() == 0) {
                return new MxCheckResult(false, null, 0);
            }

            String mxRecord = attr.get(0).toString();
            String mxHost = mxRecord.split(" ")[1].replaceAll("\\.$", "");

            return new MxCheckResult(true, mxHost, attr.size());

        } catch (NamingException e) {
            log.debug("MX lookup failed for domain: {}", domain);
            return new MxCheckResult(false, null, 0);
        }
    }

    private boolean isRoleAccount(String localPart) {
        String lower = localPart.toLowerCase();
        return ROLE_ACCOUNTS.stream()
                .anyMatch(role -> lower.equals(role) || lower.startsWith(role + "+"));
    }

    private String determineProviderType(String domain, boolean isFreeProvider) {
        if (isFreeProvider) return "FREE";
        if (domain.endsWith(".edu")) return "EDUCATION";
        if (domain.endsWith(".gov")) return "GOVERNMENT";
        return "BUSINESS";
    }

    private String suggestCorrection(String email, String domain) {
        if (COMMON_TYPOS.containsKey(domain)) {
            return email.replace(domain, COMMON_TYPOS.get(domain));
        }
        return null;
    }

    private int calculateRiskScore(boolean syntaxValid, boolean domainExists,
                                   boolean isDisposable, boolean isRoleAccount,
                                   String providerType) {
        int score = 100;

        if (!syntaxValid) score -= 100;
        if (!domainExists) score -= 80;
        if (isDisposable) score -= 60;
        if (isRoleAccount) score -= 20;
        if ("FREE".equals(providerType)) score -= 10;
        if ("BUSINESS".equals(providerType)) score += 20;

        return Math.max(0, Math.min(100, score));
    }

    @Async
    protected void saveHistory(String email, boolean isValid, int riskScore, long validationTime) {
        ValidationHistory history = new ValidationHistory();
        history.setValidationType("EMAIL");
        history.setInputValue(email);
        history.setIsValid(isValid);
        history.setRiskScore(riskScore);
        history.setValidationTimeMs(validationTime);
        validationHistoryRepository.save(history);
    }

    // Classe interne pour résultat MX
    private record MxCheckResult(boolean exists, String mxHost, int count) {}
}
