package com.eldercare.service.dto;

import java.util.List;

public record PatientJourneyResponse(
        Long patientId,
        boolean basicDetails,
        boolean isMedical,
        boolean isAdmission,
        boolean isNote,
        boolean isComplete,
        List<String> pendingSections
) {}
