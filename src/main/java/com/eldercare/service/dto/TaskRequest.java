package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record TaskRequest(
        @NotBlank(message = "Task name is required") String taskName,
        Boolean active
) {}
