package com.eldercare.service.repository;

import com.eldercare.service.entity.TaskGroupEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskGroupRepository extends JpaRepository<TaskGroupEntity, Long> {

    List<TaskGroupEntity> findByActiveTrueOrderByGroupNameAsc();

    List<TaskGroupEntity> findByGroupNameContainingIgnoreCaseAndActiveTrueOrderByGroupNameAsc(String groupName);

    Optional<TaskGroupEntity> findByGroupNameIgnoreCase(String groupName);

    boolean existsByGroupNameIgnoreCase(String groupName);
}
