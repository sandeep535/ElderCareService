package com.eldercare.service.dto;

public record RoleResponse(
        Long id,
        String roleName,
        String roleId,
        Long parentId,
        String parentRoleId,
        String parentRoleName
) {}
