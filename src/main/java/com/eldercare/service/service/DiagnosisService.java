package com.eldercare.service.service;

import com.eldercare.service.dto.DiagnosisRequest;
import com.eldercare.service.dto.DiagnosisResponse;
import com.eldercare.service.entity.DiagnosisEntity;
import com.eldercare.service.entity.DiagnosisMasterEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.DiagnosisMasterRepository;
import com.eldercare.service.repository.DiagnosisRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisMasterRepository diagnosisMasterRepository;
    private final AuditService auditService;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                            PatientRepository patientRepository,
                            DiagnosisMasterRepository diagnosisMasterRepository,
                            AuditService auditService) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.diagnosisMasterRepository = diagnosisMasterRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DiagnosisResponse add(Long patientId, DiagnosisRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        DiagnosisEntity entity = new DiagnosisEntity();
        entity.setPatient(patient);
        entity.setDiagnosisName(request.diagnosisName());
        entity.setDiagnosisBy(request.diagnosisBy());
        entity.setStatus(request.status());
        if (request.diagnosisMasterId() != null) {
            DiagnosisMasterEntity master = diagnosisMasterRepository.findById(request.diagnosisMasterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Diagnosis master", request.diagnosisMasterId()));
            entity.setDiagnosisMaster(master);
        }
        entity.setCreatedBy("system");
        diagnosisRepository.save(entity);

        DiagnosisResponse response = toResponse(entity);
        auditService.record(patientId, "DIAGNOSIS", response);
        return response;
    }

    public List<DiagnosisResponse> getByPatient(Long patientId) {
        return diagnosisRepository.findByPatientIdOrderByCreatedOnDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DiagnosisResponse update(Long patientId, Long diagnosisId, DiagnosisRequest request) {
        DiagnosisEntity entity = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis", diagnosisId));
        if (!entity.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Diagnosis", diagnosisId);
        }
        entity.setDiagnosisName(request.diagnosisName());
        entity.setDiagnosisBy(request.diagnosisBy());
        entity.setStatus(request.status());
        if (request.diagnosisMasterId() != null) {
            DiagnosisMasterEntity master = diagnosisMasterRepository.findById(request.diagnosisMasterId())
                    .orElseThrow(() -> new ResourceNotFoundException("Diagnosis master", request.diagnosisMasterId()));
            entity.setDiagnosisMaster(master);
        } else {
            entity.setDiagnosisMaster(null);
        }
        entity.setUpdatedBy("system");
        diagnosisRepository.save(entity);
        return toResponse(entity);
    }

    private DiagnosisResponse toResponse(DiagnosisEntity entity) {
        return new DiagnosisResponse(entity.getId(), entity.getPatient().getId(), entity.getDiagnosisName(),
                entity.getDiagnosisBy(), entity.getStatus(),
                entity.getDiagnosisMaster() != null ? entity.getDiagnosisMaster().getId() : null,
                entity.getDiagnosisMaster() != null ? entity.getDiagnosisMaster().getDiagnosisName() : null);
    }
}
