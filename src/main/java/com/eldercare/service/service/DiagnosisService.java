package com.eldercare.service.service;

import com.eldercare.service.dto.DiagnosisRequest;
import com.eldercare.service.dto.DiagnosisResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.DiagnosisEntity;
import com.eldercare.service.entity.DiagnosisMasterEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.DiagnosisMasterRepository;
import com.eldercare.service.repository.DiagnosisRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisMasterRepository diagnosisMasterRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AuditService auditService;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                            PatientRepository patientRepository,
                            DiagnosisMasterRepository diagnosisMasterRepository,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            AuditService auditService) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.diagnosisMasterRepository = diagnosisMasterRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
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
        UserEntity diagnosisByUser = userRepository.findByUsername(entity.getDiagnosisBy()).orElse(null);
        UserInfoResponse diagnosisByInfo = diagnosisByUser != null ? buildUserInfo(diagnosisByUser) : null;
        return new DiagnosisResponse(entity.getId(), entity.getPatient().getId(), entity.getDiagnosisName(),
                diagnosisByInfo, entity.getStatus(),
                entity.getDiagnosisMaster() != null ? entity.getDiagnosisMaster().getId() : null,
                entity.getDiagnosisMaster() != null ? entity.getDiagnosisMaster().getDiagnosisName() : null);
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
}
