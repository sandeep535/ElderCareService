package com.eldercare.service.dto;

import java.time.LocalDate;

public record NokResponse(
        Long id,
        String firstName,
        String lastName,
        String relationship,
        LocalDate dob,
        String gender,
        String phoneNumber,
        String email,
        boolean primaryContact,
        boolean canMakeMedical,
        String notes,
        Long patientId
) {}
