package com.eldercare.service.dto;

import java.time.LocalDate;
import java.util.List;

public record PatientResponse(
        Long id,
        String patientId,
        String firstName,
        String lastName,
        LocalDate dob,
        String gender,
        String profilePhotoUrl,
        String completionStatus,
        List<String> pendingSections
) {}
