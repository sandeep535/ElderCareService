package com.eldercare.service.dto;

import java.time.LocalDateTime;

public record NotesResponse(
        Long id,
        String notes,
        Long patientId,
        String createdBy,
        LocalDateTime createdOn
) {}
