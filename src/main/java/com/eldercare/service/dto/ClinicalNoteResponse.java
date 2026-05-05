package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record ClinicalNoteResponse(
        Long id,
        Long patientId,
        String noteTitle,
        LocalDateTime noteDate,
        String notes,
        String notesType,
        String notesTypeDisplay,
        String priority,
        String priorityDisplay,
        UserInfoResponse recordedBy,
        LocalDateTime createdOn
) {}
