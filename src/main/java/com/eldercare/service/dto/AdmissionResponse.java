package com.eldercare.service.dto;

import java.time.LocalDate;

public record AdmissionResponse(
        Long id,
        LocalDate admissionDate,
        String roomNumber,
        String bed,
        String status,
        String statusDisplay,
        String emrContactName,
        String phoneNumber,
        Long patientId
) {}
