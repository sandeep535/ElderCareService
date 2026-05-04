package com.eldercare.service.dto;

import java.time.LocalDate;

public record UserDetailsResponse(
        Long userId,
        String username,
        String userType,
        String firstName,
        String lastName,
        String gender,
        LocalDate dob,
        String designation,
        String email,
        String phoneNumber,
        String qualification,
        String roleId
) {}
