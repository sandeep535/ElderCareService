package com.eldercare.service.dto;

import java.time.LocalDate;

public record PatientMedicationResponse(
        Long id,
        Long patientId,
        Long medicationId,
        String medicationName,
        String dose,
        int frequency,
        int durationDays,
        LocalDate startDate,
        LocalDate endDate,
        boolean active,
        String instructions
) {}
