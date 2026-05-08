package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PatientTaskRequest(
        List<Long> taskIds,
        List<Long> taskGroupIds,
        @NotBlank(message = "Scheduled date time is required") String scheduledDateTime,
        String status,
        String notes
) {}
