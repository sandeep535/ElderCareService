package com.eldercare.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalResponse(
        Long id,
        Integer systolic,
        Integer diastolic,
        Integer hr,
        BigDecimal temp,
        Integer spo2,
        String notes,
        boolean hasAlert,
        boolean alertResolved,
        Long patientId,
        LocalDateTime recordedAt
) {}
