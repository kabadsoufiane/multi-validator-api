package com.validatorapi.service;

import com.validatorapi.model.DisposableDomain;
import com.validatorapi.repository.DisposableDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DisposableDomainService {

    private final DisposableDomainRepository repository;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String SOURCE_1 =
            "https://raw.githubusercontent.com/disposable/disposable-email-domains/master/domains.txt";
    private static final String SOURCE_2 =
            "https://raw.githubusercontent.com/disposable-email-blocked-domains/disposable-email-blocked-domains/master/disposable-email-blocked-domains.conf";

    @Scheduled(cron = "${validator.disposable.update-cron}")
    @Transactional
    public void updateDisposableDomains() {
        log.info("Starting disposable domains update...");

        try {
            Set<String> allDomains = new HashSet<>();

            // Fetch source 1
            String content1 = restTemplate.getForObject(SOURCE_1, String.class);
            if (content1 != null) {
                allDomains.addAll(Arrays.stream(content1.split("\n"))
                        .map(String::trim)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .collect(Collectors.toSet()));
            }

            log.info("Fetched {} domains from source 1", allDomains.size());

            // Suppression anciens et insertion nouveaux
            repository.deleteAll();

            Set<DisposableDomain> entities = allDomains.stream()
                    .map(domain -> new DisposableDomain(domain, "GITHUB"))
                    .collect(Collectors.toSet());

            repository.saveAll(entities);

            log.info("✅ Successfully updated {} disposable domains", entities.size());

        } catch (Exception e) {
            log.error("❌ Failed to update disposable domains", e);
        }
    }

    // Méthode pour initialisation au démarrage
    @Transactional
    public void initializeIfEmpty() {
        if (repository.count() == 0) {
            log.info("No disposable domains found, initializing...");
            updateDisposableDomains();
        }
    }
}
