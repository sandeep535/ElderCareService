package com.eldercare.service.dto;

public record MedicationMasterResponse(
        Long id,
        String drugName,
        String genericName,
        String defaultStrength,
        String defaultStrengthUnit,
        String defaultDoseForm,
        String manufacturer,
        boolean active
) {}
