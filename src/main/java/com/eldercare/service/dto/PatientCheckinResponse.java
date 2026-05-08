package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record PatientCheckinResponse(
        Long id,
        Long patientId,
        LocalDateTime checkInTime,
        LocalDateTime checkOutTime,
        String createdBy,
        LocalDateTime createdOn
) {}
