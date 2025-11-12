package com.validatorapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PhoneValidationResponse {

    private String phone;
    private Boolean valid;
    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("country_prefix")
    private Integer countryPrefix;

    @JsonProperty("national_format")
    private String nationalFormat;

    @JsonProperty("international_format")
    private String internationalFormat;

    @JsonProperty("e164_format")
    private String e164Format;

    private String type;
    private String carrier;
    private String timezone;

    @JsonProperty("risk_score")
    private Integer riskScore;

    @JsonProperty("validation_time_ms")
    private Long validationTimeMs;

    @JsonProperty("checked_at")
    private LocalDateTime checkedAt;
}
