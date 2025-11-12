package com.validatorapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class EmailValidationResponse {

    private String email;

    private Boolean valid;

    @JsonProperty("syntax_valid")
    private Boolean syntaxValid;

    @JsonProperty("domain_exists")
    private Boolean domainExists;

    @JsonProperty("mx_host")
    private String mxHost;

    @JsonProperty("mx_records_count")
    private Integer mxRecordsCount;

    @JsonProperty("is_disposable")
    private Boolean isDisposable;

    @JsonProperty("is_role_account")
    private Boolean isRoleAccount;

    @JsonProperty("is_free_provider")
    private Boolean isFreeProvider;

    @JsonProperty("provider_type")
    private String providerType; // FREE, BUSINESS, EDUCATION

    private String suggestion;

    @JsonProperty("risk_score")
    private Integer riskScore;

    @JsonProperty("validation_time_ms")
    private Long validationTimeMs;

    @JsonProperty("checked_at")
    private LocalDateTime checkedAt;
}