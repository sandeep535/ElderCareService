package com.eldercare.service.dto;

import java.time.LocalDate;

public record AdmissionRequest(
        LocalDate admissionDate,
        String roomNumber,
        String bed,
        String status,
        String emrContactName,
        String phoneNumber
) {}
