package com.eldercare.service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VitalMetricBulkUpdateRequest(

        @NotNull(message = "ID is required for bulk update")
        Long id,

        Integer deviceId,

        String fieldKey,

        String displayName,

        String unit,

        String category,

        Boolean mandatory,

        Boolean display,

        BigDecimal lowValue,

        BigDecimal highValue,

        String normalRange,

        Integer sortOrder,

        Boolean active
) {}
