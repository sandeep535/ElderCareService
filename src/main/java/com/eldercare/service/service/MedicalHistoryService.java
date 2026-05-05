package com.eldercare.service.service;

import com.eldercare.service.dto.MedicalHistoryRequest;
import com.eldercare.service.dto.MedicalHistoryResponse;
import com.eldercare.service.entity.MedicalHistoryEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.MedicalHistoryRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicalHistoryService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final MasterTableRepository masterTableRepository;
    private final AuditService auditService;

    public MedicalHistoryService(MedicalHistoryRepository medicalHistoryRepository,
                                 PatientRepository patientRepository,
                                 MasterTableRepository masterTableRepository,
                                 AuditService auditService) {
        this.medicalHistoryRepository = medicalHistoryRepository;
        this.patientRepository = patientRepository;
        this.masterTableRepository = masterTableRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MedicalHistoryResponse add(Long patientId, MedicalHistoryRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        MedicalHistoryEntity entry = new MedicalHistoryEntity();
        entry.setPatient(patient);
        mapFields(entry, request);
        entry.setCreatedBy(currentUser);
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

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        mapFields(entry, request);
        entry.setUpdatedBy(currentUser);
        medicalHistoryRepository.save(entry);
        return toResponse(entry);
    }

    private void mapFields(MedicalHistoryEntity entry, MedicalHistoryRequest request) {
        String surgeryTypeCode = null;
        if (request.surgeryTypeId() != null) {
            surgeryTypeCode = masterTableRepository.findById(request.surgeryTypeId())
                    .orElseThrow(() -> new ElderCareException("Invalid surgery type id: " + request.surgeryTypeId()))
                    .getLookupCode();
        }
        entry.setSurgeryName(request.surgeryName());
        entry.setSurgeryDate(request.surgeryDate());
        entry.setSurgeryType(surgeryTypeCode);
        entry.setSurgeon(request.surgeon());
        entry.setHospital(request.hospital());
        entry.setProcedureCode(request.procedureCode());
        entry.setNotes(request.notes());
    }

    private MedicalHistoryResponse toResponse(MedicalHistoryEntity entity) {
        String surgeryTypeDisplay = null;
        if (entity.getSurgeryType() != null) {
            surgeryTypeDisplay = masterTableRepository
                    .findByTypeAndLookupCode("SURGERY_TYPE", entity.getSurgeryType())
                    .map(m -> m.getLookupItem())
                    .orElse(entity.getSurgeryType());
        }
        return new MedicalHistoryResponse(
                entity.getId(),
                entity.getPatient().getId(),
                entity.getSurgeryName(),
                entity.getSurgeryDate(),
                entity.getSurgeryType(),
                surgeryTypeDisplay,
                entity.getSurgeon(),
                entity.getHospital(),
                entity.getProcedureCode(),
                entity.getNotes(),
                entity.getCreatedOn()
        );
    }
}
