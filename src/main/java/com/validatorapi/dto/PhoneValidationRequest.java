package com.validatorapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PhoneValidationRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String country; // Code pays ISO optionnel (FR, US, etc.)
}