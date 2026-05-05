package com.eldercare.service.service;

import com.eldercare.service.dto.DiagnosisRequest;
import com.eldercare.service.dto.DiagnosisResponse;
import com.eldercare.service.dto.UserInfoResponse;
import com.eldercare.service.entity.DiagnosisEntity;
import com.eldercare.service.entity.DiagnosisMasterEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.DiagnosisMasterRepository;
import com.eldercare.service.repository.DiagnosisRepository;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserDetailsRepository;
import com.eldercare.service.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final PatientRepository patientRepository;
    private final DiagnosisMasterRepository diagnosisMasterRepository;
    private final MasterTableRepository masterTableRepository;
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final AuditService auditService;

    public DiagnosisService(DiagnosisRepository diagnosisRepository,
                            PatientRepository patientRepository,
                            DiagnosisMasterRepository diagnosisMasterRepository,
                            MasterTableRepository masterTableRepository,
                            UserRepository userRepository,
                            UserDetailsRepository userDetailsRepository,
                            AuditService auditService) {
        this.diagnosisRepository = diagnosisRepository;
        this.patientRepository = patientRepository;
        this.diagnosisMasterRepository = diagnosisMasterRepository;
        this.masterTableRepository = masterTableRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.auditService = auditService;
    }

    @Transactional
    public DiagnosisResponse add(Long patientId, DiagnosisRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        validateStatus(request.status());

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        String diagnosisBy = currentUser;
        if (request.diagnosisByUserId() != null) {
            UserEntity diagnosisByUser = userRepository.findById(request.diagnosisByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.diagnosisByUserId()));
            diagnosisBy = diagnosisByUser.getUsername();
        }

        DiagnosisEntity entity = new DiagnosisEntity();
        entity.setPatient(patient);
        entity.setDiagnosisName(request.diagnosisName());
        entity.setDiagnosisBy(diagnosisBy);
        entity.setDiagnosisDate(request.diagnosisDate());
        entity.setNotes(request.notes());
        entity.setStatus(request.status().toUpperCase());
        entity.setDiagnosisMaster(resolveMaster(request.diagnosisMasterId()));
        entity.setCreatedBy(currentUser);
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

        validateStatus(request.status());

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        String diagnosisBy = currentUser;
        if (request.diagnosisByUserId() != null) {
            UserEntity diagnosisByUser = userRepository.findById(request.diagnosisByUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.diagnosisByUserId()));
            diagnosisBy = diagnosisByUser.getUsername();
        }

        entity.setDiagnosisName(request.diagnosisName());
        entity.setDiagnosisBy(diagnosisBy);
        entity.setDiagnosisDate(request.diagnosisDate());
        entity.setNotes(request.notes());
        entity.setStatus(request.status().toUpperCase());
        entity.setDiagnosisMaster(resolveMaster(request.diagnosisMasterId()));
        entity.setUpdatedBy(currentUser);
        diagnosisRepository.save(entity);
        return toResponse(entity);
    }

    private void validateStatus(String status) {
        if (status != null) {
            masterTableRepository.findByTypeAndLookupCode("DIAGNOSIS_STATUS", status.toUpperCase())
                    .orElseThrow(() -> new ElderCareException("Invalid diagnosis status: " + status));
        }
    }

    private DiagnosisMasterEntity resolveMaster(Long masterId) {
        if (masterId == null) return null;
        return diagnosisMasterRepository.findById(masterId)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis master", masterId));
    }

    private DiagnosisResponse toResponse(DiagnosisEntity entity) {
        UserEntity diagnosisByUser = userRepository.findByUsername(entity.getDiagnosisBy()).orElse(null);
        UserInfoResponse diagnosisByInfo = diagnosisByUser != null ? buildUserInfo(diagnosisByUser) : null;

        String statusDisplay = null;
        if (entity.getStatus() != null) {
            statusDisplay = masterTableRepository
                    .findByTypeAndLookupCode("DIAGNOSIS_STATUS", entity.getStatus())
                    .map(m -> m.getLookupItem())
                    .orElse(entity.getStatus());
        }

        DiagnosisMasterEntity master = entity.getDiagnosisMaster();
        return new DiagnosisResponse(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getDiagnosisName(),
                diagnosisByInfo,
                entity.getDiagnosisDate(),
                entity.getNotes(),
                entity.getStatus(),
                statusDisplay,
                master != null ? master.getId() : null,
                master != null ? master.getDiagnosisName() : null,
                master != null ? master.getIcdCode() : null
        );
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
