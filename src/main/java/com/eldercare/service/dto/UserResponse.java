package com.eldercare.service.dto;

import java.time.LocalDateTime;
import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String userType,
        boolean active,
        List<String> roles,
        LocalDateTime createdOn,
        LocalDateTime updatedOn
) {}
