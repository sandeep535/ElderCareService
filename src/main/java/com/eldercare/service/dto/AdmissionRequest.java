package com.eldercare.service.dto;

import java.time.LocalDate;

public record AdmissionRequest(
        Long id,
        LocalDate admissionDate,
        String roomNumber,
        String bed,
        String status,
        String emrContactName,
        String phoneNumber
) {}
