package com.eldercare.service.dto;

public record DiagnosisMasterResponse(
        Long id,
        String diagnosisName,
        String icdCode,
        boolean active
) {}
