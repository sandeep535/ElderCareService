package com.eldercare.service.dto;

public record DiagnosisResponse(
        Long id,
        Long patientId,
        String diagnosisName,
        UserInfoResponse diagnosisBy,
        String status,
        Long diagnosisMasterId,
        String diagnosisMasterName
) {}
