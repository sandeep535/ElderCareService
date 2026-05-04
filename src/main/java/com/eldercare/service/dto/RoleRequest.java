package com.eldercare.service.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRequest(
        @NotBlank(message = "Role name is required") String roleName,
        @NotBlank(message = "Role ID is required") String roleId,
        String parentRoleId
) {}
