package com.validatorapi.controller;

import com.validatorapi.dto.*;
import com.validatorapi.service.BatchValidationService;
import com.validatorapi.service.ComboValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
@Tag(name = "Batch & Combo", description = "Advanced validation endpoints")
public class BatchValidationController {

    private final BatchValidationService batchValidationService;
    private final ComboValidationService comboValidationService;

    @PostMapping("/batch/email")
    @Operation(summary = "Batch email validation",
            description = "Validate up to 1000 emails in parallel")
    public ResponseEntity<BatchEmailValidationResponse> validateBatchEmail(
            @Valid @RequestBody BatchEmailValidationRequest request) {

        BatchEmailValidationResponse response =
                batchValidationService.validateBatch(request.getEmails());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/combo")
    @Operation(summary = "Combo validation (Email + Phone)",
            description = "Validate email and phone in one request")
    public ResponseEntity<ComboValidationResponse> validateCombo(
            @Valid @RequestBody ComboValidationRequest request) {

        ComboValidationResponse response = comboValidationService.validateCombo(
                request.getEmail(),
                request.getPhone(),
                request.getCountry()
        );

        return ResponseEntity.ok(response);
    }
}