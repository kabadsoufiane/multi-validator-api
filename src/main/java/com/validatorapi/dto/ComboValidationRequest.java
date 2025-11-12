package com.validatorapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ComboValidationRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Phone is required")
    private String phone;

    private String country;
}