package com.validatorapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "disposable_domains", indexes = {
        @Index(name = "idx_domain", columnList = "domain")
})
@Data
@NoArgsConstructor
public class DisposableDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String domain;

    @Column(nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(length = 50)
    private String source; // GITHUB, MANUAL

    public DisposableDomain(String domain, String source) {
        this.domain = domain;
        this.source = source;
    }
}