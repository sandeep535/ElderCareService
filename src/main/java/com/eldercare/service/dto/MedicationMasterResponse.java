package com.eldercare.service.dto;

public record MedicationMasterResponse(
        Long id,
        String name,
        String genericName,
        String strength,
        String form,
        boolean active
) {}
