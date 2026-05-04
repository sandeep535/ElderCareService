package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record AuditFailureResponse(
        Long id,
        LocalDateTime createdOn,
        String typeScreen,
        String dataJson,
        Long patientId,
        String failureReason,
        boolean resolved,
        int retryCount
) {}
