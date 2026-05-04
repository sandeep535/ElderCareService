package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record NotesRequest(
        Long id,
        @NotBlank(message = "Notes cannot be empty") String notes
) {}
