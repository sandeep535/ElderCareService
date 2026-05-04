package com.eldercare.service.repository;

import com.eldercare.service.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findByRoleId(String roleId);

    boolean existsByRoleId(String roleId);

    @Query("SELECT r FROM RoleEntity r LEFT JOIN FETCH r.parent ORDER BY r.id")
    List<RoleEntity> findAllWithParent();
}
