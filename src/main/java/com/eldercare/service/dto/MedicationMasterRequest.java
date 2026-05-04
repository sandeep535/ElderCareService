package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record MedicationMasterRequest(
        @NotBlank(message = "Medication name is required") String name,
        String genericName,
        String strength,
        String form,
        Boolean active
) {}
