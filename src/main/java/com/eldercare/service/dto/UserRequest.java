package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank(message = "Username is required") String username,
        String password,
        @NotBlank(message = "User type is required") String userType,
        @NotBlank(message = "Role id is required") String roleId,
        Boolean active
) {}
