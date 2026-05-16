package com.eldercare.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ThirdPartyVitalRequest(
        LocalDateTime timestamp,
        @JsonProperty("patient_id")
        String patientId,
        List<VitalReading> readings
) {
    public record VitalReading(
            @JsonProperty("device_id")
            Integer deviceId,
            Object value,
            String units
    ) {}
}