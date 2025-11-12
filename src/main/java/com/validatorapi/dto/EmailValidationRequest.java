package com.validatorapi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailValidationRequest {

    @NotBlank(message = "Email is required")
    private String email;
}
