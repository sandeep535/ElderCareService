package com.eldercare.service.service;

import com.eldercare.service.dto.AuditFailureResponse;
import com.eldercare.service.dto.AuditLogResponse;
import com.eldercare.service.entity.AuditFailureEntity;
import com.eldercare.service.entity.AuditLogEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AuditFailureRepository;
import com.eldercare.service.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditFailureRepository auditFailureRepository;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           AuditFailureRepository auditFailureRepository) {
        this.auditLogRepository = auditLogRepository;
        this.auditFailureRepository = auditFailureRepository;
    }

    public List<AuditLogResponse> getByPatient(Long patientId, String type) {
        return (type == null || type.isBlank())
                ? auditLogRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                        .stream().map(this::toResponse).toList()
                : auditLogRepository.findByPatientIdAndTypeScreenOrderByCreatedOnDesc(patientId, type)
                        .stream().map(this::toResponse).toList();
    }

    public List<AuditFailureResponse> getPendingFailures() {
        return auditFailureRepository.findByResolvedFalseOrderByCreatedOnDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public AuditFailureResponse resolveFailure(Long id) {
        AuditFailureEntity failure = auditFailureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit failure", id));
        failure.setResolved(true);
        auditFailureRepository.save(failure);
        return toResponse(failure);
    }

    private AuditLogResponse toResponse(AuditLogEntity entity) {
        return new AuditLogResponse(entity.getId(), entity.getCreatedOn(), entity.getTypeScreen(),
                entity.getDataJson(), entity.getPatientId(), entity.getActionBy());
    }

    private AuditFailureResponse toResponse(AuditFailureEntity entity) {
        return new AuditFailureResponse(entity.getId(), entity.getCreatedOn(), entity.getTypeScreen(),
                entity.getDataJson(), entity.getPatientId(), entity.getFailureReason(),
                entity.isResolved(), entity.getRetryCount());
    }
}
