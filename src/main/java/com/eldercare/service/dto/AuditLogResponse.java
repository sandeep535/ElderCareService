package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        LocalDateTime createdOn,
        String typeScreen,
        String dataJson,
        Long patientId,
        String actionBy
) {}
