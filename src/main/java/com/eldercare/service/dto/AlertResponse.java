package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record AlertResponse(
        Long id,
        Long patientDbId,
        String patientId,
        String patientName,
        String typeOfScreen,
        String name,
        String value,
        String reason,
        String priority,
        boolean resolved,
        String createdBy,
        LocalDateTime createdOn,
        String updatedBy,
        LocalDateTime updatedOn
) {}
