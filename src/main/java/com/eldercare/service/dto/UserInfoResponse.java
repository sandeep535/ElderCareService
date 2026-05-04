package com.eldercare.service.dto;

public record UserInfoResponse(
        Long userId,
        String username,
        String userType,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        String designation
) {}
