package com.eldercare.service.service;

import com.eldercare.service.entity.AuditFailureEntity;
import com.eldercare.service.entity.AuditLogEntity;
import com.eldercare.service.repository.AuditFailureRepository;
import com.eldercare.service.repository.AuditLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger log = LogManager.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final AuditFailureRepository auditFailureRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository,
                        AuditFailureRepository auditFailureRepository,
                        ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.auditFailureRepository = auditFailureRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Fire-and-forget async audit write.
     * Caller gets response immediately — audit happens in background.
     * If it fails, the payload is saved to audit_failure table.
     *
     * @param patientId  the patient this action relates to
     * @param typeScreen module name e.g. MEDICAL, VITALS, ADMISSION
     * @param data       the object to serialize as data_json (only that module's data)
     */
    @Async
    public void record(Long patientId, String typeScreen, Object data) {
        String actionBy = resolveCurrentUser();
        String dataJson = null;

        try {
            dataJson = objectMapper.writeValueAsString(data);

            AuditLogEntity audit = new AuditLogEntity();
            audit.setPatientId(patientId);
            audit.setTypeScreen(typeScreen);
            audit.setDataJson(dataJson);
            audit.setActionBy(actionBy);
            auditLogRepository.save(audit);

            log.debug("Audit recorded: patient={} type={}", patientId, typeScreen);

        } catch (Exception e) {
            log.error("Audit write failed for patient={} type={} reason={}", patientId, typeScreen, e.getMessage());
            saveFailure(patientId, typeScreen, dataJson != null ? dataJson : data.toString(), e.getMessage());
        }
    }

    private void saveFailure(Long patientId, String typeScreen, String dataJson, String reason) {
        try {
            AuditFailureEntity failure = new AuditFailureEntity();
            failure.setPatientId(patientId);
            failure.setTypeScreen(typeScreen);
            failure.setDataJson(dataJson);
            failure.setFailureReason(reason);
            auditFailureRepository.save(failure);
        } catch (Exception ex) {
            log.error("CRITICAL: Could not save audit failure record. patientId={} reason={}", patientId, ex.getMessage());
        }
    }

    private String resolveCurrentUser() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }
}
