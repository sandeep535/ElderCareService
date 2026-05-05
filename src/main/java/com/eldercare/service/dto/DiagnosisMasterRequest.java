package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record DiagnosisMasterRequest(
        @NotBlank(message = "Diagnosis name is required") String diagnosisName,
        String icdCode,
        Boolean active
) {}
