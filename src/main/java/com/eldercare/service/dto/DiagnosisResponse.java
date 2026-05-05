package com.eldercare.service.dto;

import java.time.LocalDate;

public record DiagnosisResponse(
        Long id,
        Long patientId,
        String diagnosisName,
        UserInfoResponse diagnosisBy,
        LocalDate diagnosisDate,
        String notes,
        String status,
        String statusDisplay,
        Long diagnosisMasterId,
        String diagnosisMasterName,
        String icdCode
) {}
