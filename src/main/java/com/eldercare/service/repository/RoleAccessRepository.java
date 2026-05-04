package com.eldercare.service.repository;

import com.eldercare.service.entity.RoleAccessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoleAccessRepository extends JpaRepository<RoleAccessEntity, Long> {

    @Query("SELECT ra FROM RoleAccessEntity ra JOIN FETCH ra.role WHERE ra.user.id = :userId")
    List<RoleAccessEntity> findByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndRoleId(Long userId, Long roleId);
}
