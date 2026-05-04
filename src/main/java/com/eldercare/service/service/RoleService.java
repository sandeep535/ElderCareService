package com.eldercare.service.service;

import com.eldercare.service.dto.RoleRequest;
import com.eldercare.service.dto.RoleResponse;
import com.eldercare.service.entity.RoleEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.repository.RoleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    private static final Logger log = LogManager.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAllWithParent().stream()
                .map(this::toResponse)
                .toList();
    }

    public RoleResponse createRole(RoleRequest request) {
        String roleId = request.roleId().toUpperCase().startsWith("ROLE_")
                ? request.roleId().toUpperCase()
                : "ROLE_" + request.roleId().toUpperCase();

        if (roleRepository.existsByRoleId(roleId)) {
            throw new ElderCareException("Role already exists: " + roleId);
        }

        RoleEntity role = new RoleEntity();
        role.setRoleName(request.roleName());
        role.setRoleId(roleId);
        role.setCreatedBy("admin");

        if (request.parentRoleId() != null && !request.parentRoleId().isBlank()) {
            String parentRoleId = request.parentRoleId().toUpperCase().startsWith("ROLE_")
                    ? request.parentRoleId().toUpperCase()
                    : "ROLE_" + request.parentRoleId().toUpperCase();

            RoleEntity parent = roleRepository.findByRoleId(parentRoleId)
                    .orElseThrow(() -> new ElderCareException("Parent role not found: " + parentRoleId));
            role.setParent(parent);
        }

        roleRepository.save(role);
        log.info("Role created: {} with parent: {}", roleId, request.parentRoleId());
        return toResponse(role);
    }

    private RoleResponse toResponse(RoleEntity r) {
        return new RoleResponse(
                r.getId(),
                r.getRoleName(),
                r.getRoleId(),
                r.getParent() != null ? r.getParent().getId() : null,
                r.getParent() != null ? r.getParent().getRoleId() : null,
                r.getParent() != null ? r.getParent().getRoleName() : null
        );
    }
}
