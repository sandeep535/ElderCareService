package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record MedicalHistoryResponse(
        Long id,
        Long patientId,
        String type,
        String procedureCode,
        String description,
        LocalDateTime createdOn
) {}
