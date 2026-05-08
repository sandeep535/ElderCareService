package com.eldercare.service.dto;

public record PatientTaskSummaryResponse(
        Long patientId,
        long pendingCount,
        long completedCount,
        long totalCount
) {}
