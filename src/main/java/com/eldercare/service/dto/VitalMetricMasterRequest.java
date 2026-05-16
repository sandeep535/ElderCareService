package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record VitalMetricMasterRequest(

        @NotNull(message = "Device ID is required")
        Integer deviceId,

        @NotBlank(message = "Field key is required")
        String fieldKey,

        @NotBlank(message = "Display name is required")
        String displayName,

        String unit,

        String category,

        // Whether this field must be filled when capturing vitals
        Boolean mandatory,

        // Whether to show this metric on the vitals display screen
        Boolean display,

        // Alert threshold — low (alert if value < lowValue)
        BigDecimal lowValue,

        // Alert threshold — high (alert if value > highValue)
        BigDecimal highValue,

        // Human-readable normal range label e.g. "90 - 139 mmHg"
        String normalRange,

        Integer sortOrder,

        Boolean active
) {}
