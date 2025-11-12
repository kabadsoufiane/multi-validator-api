package com.validatorapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ComboValidationResponse {

    @JsonProperty("email_validation")
    private EmailValidationResponse emailValidation;

    @JsonProperty("phone_validation")
    private PhoneValidationResponse phoneValidation;

    @JsonProperty("overall_risk_score")
    private Integer overallRiskScore;

    @JsonProperty("validation_time_ms")
    private Long validationTimeMs;

    @JsonProperty("checked_at")
    private LocalDateTime checkedAt;
}