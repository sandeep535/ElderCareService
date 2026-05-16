package com.eldercare.service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VitalResponse(
        Long id,
        
        // Body Composition Fields
        BigDecimal height,
        BigDecimal weight,
        BigDecimal bmi,
        BigDecimal bodyFatPercentage,
        BigDecimal bodyFatMass,
        BigDecimal skeletalMusclePercentage,
        BigDecimal bodyWaterPercentage,
        BigDecimal totalMoisture,
        BigDecimal extracellularWaterPct,
        BigDecimal intracellularWaterPct,
        Integer basalMetabolism,
        BigDecimal visceralFatLevel,
        BigDecimal protein,
        BigDecimal mineral,
        Integer bodyAge,
        Integer overall,
        
        // Clinical Vitals Fields
        BigDecimal temperature,
        Integer systolic,
        Integer diastolic,
        Integer bpHeartRate,
        Integer spo2,
        Integer spo2HeartRate,
        
        String notes,
        boolean hasAlert,
        boolean alertResolved,
        Long patientId,
        LocalDateTime recordedAt
) {}
