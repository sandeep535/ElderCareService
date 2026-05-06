package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicationMasterRequest(
        @NotBlank(message = "Drug name is required") String drugName,
        String genericName,
        String defaultStrength,
        String defaultStrengthUnit,
        String defaultDoseForm,
        String manufacturer,
        Boolean active
) {}
