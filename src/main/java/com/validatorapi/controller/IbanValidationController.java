package com.validatorapi.controller;

import com.validatorapi.dto.IbanValidationRequest;
import com.validatorapi.dto.IbanValidationResponse;
import com.validatorapi.service.IbanValidatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
@Tag(name = "IBAN Validation", description = "IBAN validation endpoints")
public class IbanValidationController {

    private final IbanValidatorService ibanValidatorService;

    @PostMapping("/iban")
    @Operation(summary = "Validate IBAN",
            description = "Validates IBAN format for 89 countries")
    public ResponseEntity<IbanValidationResponse> validateIban(
            @Valid @RequestBody IbanValidationRequest request) {

        IbanValidationResponse response = ibanValidatorService.validate(request.getIban());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/iban")
    @Operation(summary = "Validate IBAN via GET")
    public ResponseEntity<IbanValidationResponse> validateIbanGet(
            @RequestParam String iban) {

        IbanValidationResponse response = ibanValidatorService.validate(iban);
        return ResponseEntity.ok(response);
    }
}