package com.eldercare.service.dto;

public record TaskResponse(
        Long id,
        String taskName,
        boolean active
) {}
