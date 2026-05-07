package com.eldercare.service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

public record PatientTaskRequest(
        List<Long> taskIds,
        List<Long> taskGroupIds,
        @NotNull(message = "Scheduled date time is required") LocalDateTime scheduledDateTime,
        String status,
        String notes,
        Long assignedToUserId
) {}
