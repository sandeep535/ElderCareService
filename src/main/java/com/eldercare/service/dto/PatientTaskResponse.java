package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record PatientTaskResponse(
        Long id,
        Long patientId,
        Long taskId,
        String taskName,
        Long taskGroupId,
        String taskGroupName,
        LocalDateTime scheduledDateTime,
        String status,
        String notes,
        LocalDateTime createdOn,
        String createdBy
) {}
