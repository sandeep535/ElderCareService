package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClinicalNoteRequest(
        @NotBlank(message = "Notes cannot be empty") String notes,
        @NotBlank(message = "Notes type is required") String notesType,
        @NotBlank(message = "Priority is required") String priority
) {}
