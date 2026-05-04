package com.eldercare.service.dto;

import java.time.LocalDate;

public record UserDetailsRequest(
        String firstName,
        String lastName,
        String gender,
        LocalDate dob,
        String designation,
        String email,
        String phoneNumber,
        String qualification
) {}
