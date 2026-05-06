package com.eldercare.service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record PatientMedicationRequest(
        @NotNull(message = "Medication id is required") Long medicationMasterId,
        String rxNorm,
        String orderPriority,
        String indication,
        String strengthValue,
        String strengthUnit,
        String doseForm,
        String doseAmount,
        String route,
        String frequency,
        String prnReason,
        String prnMaxDose,
        String ivRate,
        String ivRateUnit,
        String ivVolume,
        LocalDateTime startDateTime,
        LocalDateTime stopDateTime,
        String duration,
        String orderingProvider,
        String sig,
        String adminInstructions,
        String pharmacyComments,
        boolean ackAllergiesReviewed,
        boolean ackDupeReviewed
) {}
