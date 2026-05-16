package com.eldercare.service.dto;

import java.math.BigDecimal;

public record VitalRequest(
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
        
        String notes
) {}
