package com.eldercare.service.repository;

import com.eldercare.service.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long> {

    List<AuditLogEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);

    List<AuditLogEntity> findByPatientIdAndTypeScreenOrderByCreatedOnDesc(Long patientId, String typeScreen);
}
