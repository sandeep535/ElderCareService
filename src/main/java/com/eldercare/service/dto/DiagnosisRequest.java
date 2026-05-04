package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiagnosisRequest(
        @NotBlank(message = "Diagnosis name is required") String diagnosisName,
        String diagnosisBy,
        @NotBlank(message = "Status is required") String status,
        Long diagnosisMasterId
) {}
