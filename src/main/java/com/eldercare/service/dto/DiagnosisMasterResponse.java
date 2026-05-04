package com.eldercare.service.dto;

public record DiagnosisMasterResponse(
        Long id,
        String diagnosisName,
        boolean active
) {}
