package com.eldercare.service.repository;

import com.eldercare.service.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);

    List<AuditLogEntity> findByPatientIdAndTypeScreenOrderByCreatedOnDesc(Long patientId, String typeScreen);

    List<AuditLogEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId, Pageable pageable);

    List<AuditLogEntity> findByPatientIdAndCreatedOnBetweenOrderByCreatedOnDesc(Long patientId, LocalDateTime from, LocalDateTime to);
}
