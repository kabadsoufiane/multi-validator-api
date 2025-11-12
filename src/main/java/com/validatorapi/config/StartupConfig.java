package com.validatorapi.config;

import com.validatorapi.service.DisposableDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StartupConfig {

    private final DisposableDomainService disposableDomainService;

    @Bean
    public CommandLineRunner initializeData() {
        return args -> {
            log.info("🚀 Starting application initialization...");
            disposableDomainService.initializeIfEmpty();
            log.info("✅ Initialization complete");
        };
    }
}
