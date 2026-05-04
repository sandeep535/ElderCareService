package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record ClinicalNoteResponse(
        Long id,
        Long patientId,
        String notes,
        String notesType,
        String priority,
        UserInfoResponse recordedBy,
        LocalDateTime createdOn
) {}
