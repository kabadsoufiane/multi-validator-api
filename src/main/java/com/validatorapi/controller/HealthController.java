package com.validatorapi.controller;

import com.validatorapi.model.DisposableDomain;
import com.validatorapi.repository.DisposableDomainRepository;
import com.validatorapi.repository.ValidationHistoryRepository;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
@Hidden // Cache de Swagger pour ne pas polluer la doc
public class HealthController {

    private final DisposableDomainRepository disposableDomainRepository;
    private final ValidationHistoryRepository validationHistoryRepository;

    @GetMapping
    public Map<String, Object> getStats() {
        return Map.of(
                "disposable_domains_count", disposableDomainRepository.count(),
                "total_validations", validationHistoryRepository.count(),
                "status", "healthy"
        );
    }

    @PostMapping("/add-disposable/{domain}")
    public Map<String, Object> addDisposable(@PathVariable String domain) {
        domain = domain.toLowerCase();

        if (disposableDomainRepository.existsByDomain(domain)) {
            return Map.of(
                    "status", "already_exists",
                    "domain", domain
            );
        }

        DisposableDomain newDomain = new DisposableDomain(domain, "MANUAL");
        disposableDomainRepository.save(newDomain);

        return Map.of(
                "status", "added",
                "domain", domain,
                "total_count", disposableDomainRepository.count()
        );
    }

}
