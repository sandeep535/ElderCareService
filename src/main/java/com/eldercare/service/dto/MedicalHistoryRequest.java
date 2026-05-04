package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MedicalHistoryRequest(
        @NotBlank(message = "Type is required") String type,
        String procedureCode,
        @NotBlank(message = "Description is required") String description
) {}
