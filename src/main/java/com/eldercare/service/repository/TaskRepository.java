package com.eldercare.service.repository;

import com.eldercare.service.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    List<TaskEntity> findByActiveTrueOrderByTaskNameAsc();

    List<TaskEntity> findByTaskNameContainingIgnoreCaseAndActiveTrueOrderByTaskNameAsc(String taskName);

    Optional<TaskEntity> findByTaskNameIgnoreCase(String taskName);

    boolean existsByTaskNameIgnoreCase(String taskName);
}
