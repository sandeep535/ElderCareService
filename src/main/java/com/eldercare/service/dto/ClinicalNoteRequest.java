package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ClinicalNoteRequest(
        String noteTitle,
        LocalDateTime noteDate,
        @NotBlank(message = "Notes cannot be empty") String notes,
        @NotNull(message = "Notes type is required") Long notesTypeId,
        @NotNull(message = "Priority is required") Long priorityId,
        Long recordedById
) {}
