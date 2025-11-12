package com.validatorapi.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class BatchEmailValidationRequest {

    @NotEmpty(message = "Email list cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 emails per batch")
    private List<String> emails;
}