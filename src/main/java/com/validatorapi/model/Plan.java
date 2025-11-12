package com.validatorapi.model;

import lombok.Getter;

@Getter
public enum Plan {
    FREE(10, 500),      // 10 req/min, 500/mois
    STARTER(50, 5000),  // 50 req/min, 5000/mois
    PRO(200, 50000),    // 200 req/min, 50000/mois
    BUSINESS(1000, 250000); // 1000 req/min, 250000/mois

    private final int requestsPerMinute;
    private final int requestsPerMonth;

    Plan(int requestsPerMinute, int requestsPerMonth) {
        this.requestsPerMinute = requestsPerMinute;
        this.requestsPerMonth = requestsPerMonth;
    }
}