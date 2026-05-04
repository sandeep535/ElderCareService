package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record SignupRequest(
        @NotBlank(message = "Username is required") String username,
        @NotBlank(message = "Password is required") String password,
        @NotBlank(message = "User type is required") String userType,
        @NotBlank(message = "Role ID is required") String roleId,
        String firstName,
        String lastName,
        String gender,
        LocalDate dob,
        String designation,
        String email,
        String phoneNumber,
        String qualification
) {}
