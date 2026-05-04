package com.eldercare.service.config;

import com.eldercare.service.repository.RoleRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.access.hierarchicalroles.NullRoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.stereotype.Component;

@Component
public class RoleHierarchyLoader {

    private static final Logger log = LogManager.getLogger(RoleHierarchyLoader.class);

    private final RoleRepository roleRepository;

    public RoleHierarchyLoader(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public RoleHierarchy build() {
        var roles = roleRepository.findAllWithParent();

        StringBuilder hierarchy = new StringBuilder();
        roles.stream()
                .filter(r -> r.getParent() != null)
                .forEach(r -> hierarchy
                        .append(r.getParent().getRoleId())
                        .append(" > ")
                        .append(r.getRoleId())
                        .append("\n"));

        String hierarchyStr = hierarchy.toString().trim();

        if (hierarchyStr.isEmpty()) {
            log.warn("No role hierarchy found in DB — all roles are standalone");
            return new NullRoleHierarchy();
        }

        log.info("Role hierarchy loaded from DB:\n{}", hierarchyStr);
        return RoleHierarchyImpl.fromHierarchy(hierarchyStr);
    }
}
