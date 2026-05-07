package com.eldercare.service.dto;

import java.util.List;

public record TaskGroupResponse(
        Long id,
        String groupName,
        boolean active,
        List<TaskResponse> tasks
) {}
