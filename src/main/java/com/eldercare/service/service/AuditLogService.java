package com.eldercare.service.service;

import com.eldercare.service.dto.AuditFailureResponse;
import com.eldercare.service.dto.AuditLogResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.AuditFailureEntity;
import com.eldercare.service.entity.AuditLogEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AuditFailureRepository;
import com.eldercare.service.repository.AuditLogRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditFailureRepository auditFailureRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;

    public AuditLogService(AuditLogRepository auditLogRepository,
                           AuditFailureRepository auditFailureRepository,
                           UserRepository userRepository,
                           UserDetailsRepository userDetailsRepository) {
        this.auditLogRepository = auditLogRepository;
        this.auditFailureRepository = auditFailureRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
    }

    public List<AuditLogResponse> getByPatient(Long patientId, String type) {
        return (type == null || type.isBlank())
                ? auditLogRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                        .stream().map(this::toResponse).toList()
                : auditLogRepository.findByPatientIdAndTypeScreenOrderByCreatedOnDesc(patientId, type)
                        .stream().map(this::toResponse).toList();
    }

    public List<AuditLogResponse> getLastN(Long patientId, int count) {
        return auditLogRepository.findByPatientIdOrderByCreatedOnDesc(
                        patientId, PageRequest.of(0, count))
                .stream().map(this::toResponse).toList();
    }

    public List<AuditLogResponse> getByDateRange(Long patientId, LocalDate from, LocalDate to) {
        LocalDateTime fromDt = from.atStartOfDay();
        LocalDateTime toDt = to.atTime(23, 59, 59);
        return auditLogRepository.findByPatientIdAndCreatedOnBetweenOrderByCreatedOnDesc(
                        patientId, fromDt, toDt)
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
        UserEntity actionByUser = userRepository.findByUsername(entity.getActionBy()).orElse(null);
        UserInfoResponse actionByInfo = actionByUser != null ? buildUserInfo(actionByUser) : null;
        return new AuditLogResponse(entity.getId(), entity.getCreatedOn(), entity.getTypeScreen(),
                entity.getDataJson(), entity.getPatientId(), actionByInfo);
    }

    private UserInfoResponse buildUserInfo(UserEntity user) {
        var userDetails = userDetailsRepository.findByUserId(user.getId()).orElse(null);
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getUserType(),
                userDetails != null ? userDetails.getFirstName() : null,
                userDetails != null ? userDetails.getLastName() : null,
                userDetails != null ? userDetails.getEmail() : null,
                userDetails != null ? userDetails.getPhoneNumber() : null,
                userDetails != null ? userDetails.getDesignation() : null
        );
    }

    private AuditFailureResponse toResponse(AuditFailureEntity entity) {
        return new AuditFailureResponse(entity.getId(), entity.getCreatedOn(), entity.getTypeScreen(),
                entity.getDataJson(), entity.getPatientId(), entity.getFailureReason(),
                entity.isResolved(), entity.getRetryCount());
    }
}
