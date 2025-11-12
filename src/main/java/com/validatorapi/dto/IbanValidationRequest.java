package com.validatorapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IbanValidationRequest {

    @NotBlank(message = "IBAN is required")
    private String iban;
}