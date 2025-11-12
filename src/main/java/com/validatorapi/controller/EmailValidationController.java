package com.validatorapi.controller;

import com.validatorapi.dto.EmailValidationRequest;
import com.validatorapi.dto.EmailValidationResponse;
import com.validatorapi.service.EmailValidatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
@Tag(name = "Email Validation", description = "Email validation endpoints")
public class EmailValidationController {

    private final EmailValidatorService emailValidatorService;

    @PostMapping("/email")
    @Operation(summary = "Validate email address",
            description = "Validates email syntax, domain, MX records, and detects disposable emails")
    public ResponseEntity<EmailValidationResponse> validateEmail(
            @Valid @RequestBody EmailValidationRequest request) {

        EmailValidationResponse response = emailValidatorService.validate(request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/email")
    @Operation(summary = "Validate email via GET",
            description = "Alternative GET endpoint for simple validation")
    public ResponseEntity<EmailValidationResponse> validateEmailGet(
            @RequestParam String email) {

        EmailValidationResponse response = emailValidatorService.validate(email);
        return ResponseEntity.ok(response);
    }
}
