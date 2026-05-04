package com.eldercare.service.dto;

import java.math.BigDecimal;

public record VitalRequest(
        Integer systolic,
        Integer diastolic,
        Integer hr,
        BigDecimal temp,
        Integer spo2,
        String notes
) {}
