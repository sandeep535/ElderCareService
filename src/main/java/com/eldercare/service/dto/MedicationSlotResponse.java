package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record MedicationSlotResponse(
        Long id,
        Long patientMedicationId,
        Long patientId,
        String medicationName,
        String dose,
        LocalDateTime scheduledTime,
        String status,
        LocalDateTime givenAt,
        UserInfoResponse givenBy,
        String notes
) {}
