package com.validatorapi.controller;

import com.validatorapi.dto.PhoneValidationRequest;
import com.validatorapi.dto.PhoneValidationResponse;
import com.validatorapi.service.PhoneValidatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/validate")
@RequiredArgsConstructor
@Tag(name = "Phone Validation", description = "Phone number validation endpoints")
public class PhoneValidationController {

    private final PhoneValidatorService phoneValidatorService;

    @PostMapping("/phone")
    @Operation(summary = "Validate phone number",
            description = "Validates phone number format for 180+ countries")
    public ResponseEntity<PhoneValidationResponse> validatePhone(
            @Valid @RequestBody PhoneValidationRequest request) {

        PhoneValidationResponse response = phoneValidatorService.validate(
                request.getPhone(),
                request.getCountry()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/phone")
    @Operation(summary = "Validate phone via GET")
    public ResponseEntity<PhoneValidationResponse> validatePhoneGet(
            @RequestParam String phone,
            @RequestParam(required = false) String country) {

        PhoneValidationResponse response = phoneValidatorService.validate(phone, country);
        return ResponseEntity.ok(response);
    }
}