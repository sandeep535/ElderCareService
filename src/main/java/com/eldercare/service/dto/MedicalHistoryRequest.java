package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record MedicalHistoryRequest(
        @NotBlank(message = "Surgery name is required") String surgeryName,
        LocalDate surgeryDate,
        Long surgeryTypeId,
        String surgeon,
        String hospital,
        String procedureCode,
        String notes
) {}
