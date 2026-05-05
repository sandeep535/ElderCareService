package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record NotesResponse(
        Long id,
        String notes,
        String noteType,
        String noteTypeDisplay,
        Long patientId,
        UserInfoResponse createdBy,
        LocalDateTime createdOn
) {}
