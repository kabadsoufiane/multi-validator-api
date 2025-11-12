package com.validatorapi.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "validation_history")
@Data
@NoArgsConstructor
public class ValidationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String validationType; // EMAIL, PHONE, IBAN

    @Column(nullable = false)
    private String inputValue;

    @Column(nullable = false)
    private Boolean isValid;

    @Column
    private Integer riskScore;

    @Column
    private Long validationTimeMs;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 100)
    private String apiKey; // Pour tracking par client
}