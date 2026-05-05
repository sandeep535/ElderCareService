package com.eldercare.service.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MedicalHistoryResponse(
        Long id,
        Long patientId,
        String surgeryName,
        LocalDate surgeryDate,
        String surgeryType,
        String surgeryTypeDisplay,
        String surgeon,
        String hospital,
        String procedureCode,
        String notes,
        LocalDateTime createdOn
) {}
