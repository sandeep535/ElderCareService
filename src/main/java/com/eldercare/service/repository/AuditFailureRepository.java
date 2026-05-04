package com.eldercare.service.repository;

import com.eldercare.service.entity.AuditFailureEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditFailureRepository extends JpaRepository<AuditFailureEntity, Long> {

    List<AuditFailureEntity> findByResolvedFalseOrderByCreatedOnDesc();
}
