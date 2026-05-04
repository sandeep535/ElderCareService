package com.eldercare.service.service;

import com.eldercare.service.dto.MedicalHistoryRequest;
import com.eldercare.service.dto.MedicalHistoryResponse;
import com.eldercare.service.entity.MedicalHistoryEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MedicalHistoryRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public MedicalHistoryService(MedicalHistoryRepository medicalHistoryRepository,
                                 PatientRepository patientRepository,
                                 AuditService auditService) {
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.patientRepository = patientRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MedicalHistoryResponse add(Long patientId, MedicalHistoryRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        MedicalHistoryEntity entry = new MedicalHistoryEntity();
        entry.setPatient(patient);
        entry.setType(request.type());
        entry.setProcedureCode(request.procedureCode());
        entry.setDescription(request.description());
        entry.setCreatedBy("system");
        medicalHistoryRepository.save(entry);

        MedicalHistoryResponse response = toResponse(entry);
        auditService.record(patientId, "MEDICAL_HISTORY", response);
        return response;
    }

    public List<MedicalHistoryResponse> getByPatient(Long patientId) {
        return medicalHistoryRepository.findByPatientIdOrderByCreatedOnDesc(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MedicalHistoryResponse update(Long patientId, Long historyId, MedicalHistoryRequest request) {
        MedicalHistoryEntity entry = medicalHistoryRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Medical history entry", historyId));
        if (!entry.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Medical history entry", historyId);
        }
        entry.setType(request.type());
        entry.setProcedureCode(request.procedureCode());
        entry.setDescription(request.description());
        entry.setUpdatedBy("system");
        medicalHistoryRepository.save(entry);
        return toResponse(entry);
    }

    private MedicalHistoryResponse toResponse(MedicalHistoryEntity entity) {
        return new MedicalHistoryResponse(entity.getId(), entity.getPatient().getId(),
                entity.getType(), entity.getProcedureCode(), entity.getDescription(), entity.getCreatedOn());
    }
}
