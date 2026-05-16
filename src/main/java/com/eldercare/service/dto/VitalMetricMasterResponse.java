package com.eldercare.service.dto;

import java.math.BigDecimal;

public record VitalMetricMasterResponse(
        Long id,
        Integer deviceId,
        String fieldKey,
        String displayName,
        String unit,
        String category,
        boolean mandatory,
        boolean display,
        BigDecimal lowValue,
        BigDecimal highValue,
        String normalRange,
        Integer sortOrder,
        boolean active
) {}
