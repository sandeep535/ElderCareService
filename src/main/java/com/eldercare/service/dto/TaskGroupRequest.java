package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record TaskGroupRequest(
        @NotBlank(message = "Group name is required") String groupName,
        List<Long> taskIds,
        Boolean active
) {}
