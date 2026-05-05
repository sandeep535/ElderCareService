package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record DiagnosisRequest(
        @NotBlank(message = "Diagnosis name is required") String diagnosisName,
        @NotBlank(message = "Status is required") String status,
        LocalDate diagnosisDate,
        String notes,
        Long diagnosisMasterId,
        Long diagnosisByUserId
) {}
