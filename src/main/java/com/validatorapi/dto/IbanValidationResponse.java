package com.validatorapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class IbanValidationResponse {

    private String iban;
    private Boolean valid;
    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    @JsonProperty("check_digits")
    private String checkDigits;

    @JsonProperty("bank_code")
    private String bankCode;

    @JsonProperty("branch_code")
    private String branchCode;

    @JsonProperty("account_number")
    private String accountNumber;

    private String bic;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonProperty("iban_formatted")
    private String ibanFormatted;

    @JsonProperty("validation_time_ms")
    private Long validationTimeMs;

    @JsonProperty("checked_at")
    private LocalDateTime checkedAt;
}