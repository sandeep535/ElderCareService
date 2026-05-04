package com.eldercare.service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record PatientMedicationRequest(
        @NotNull(message = "Medication id is required") Long medicationId,
        @NotBlank(message = "Dose is required") String dose,
        @Min(value = 1, message = "Frequency must be at least 1") int frequency,
        @Min(value = 1, message = "Duration days must be at least 1") int durationDays,
        @NotNull(message = "Start date is required") LocalDate startDate,
        String instructions,
        List<String> slotCodes
) {}
