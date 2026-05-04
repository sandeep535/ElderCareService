package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record NokRequest(
        Long id,
        @NotBlank(message = "First name is required") String firstName,
        String lastName,
        String relationship,
        LocalDate dob,
        String gender,
        String phoneNumber,
        String email,
        boolean primaryContact,
        boolean canMakeMedical,
        String notes
) {}
