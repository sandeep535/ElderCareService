package com.eldercare.service.service;

import com.eldercare.service.dto.AdmissionRequest;
import com.eldercare.service.dto.AdmissionResponse;
import com.eldercare.service.entity.AdmissionEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AdmissionRepository;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdmissionService {

    private static final Logger log = LogManager.getLogger(AdmissionService.class);

    private final AdmissionRepository admissionRepository;
    private final PatientRepository patientRepository;
    private final PatientJourneyRepository journeyRepository;
    private final MasterTableRepository masterTableRepository;
    private final AuditService auditService;

    public AdmissionService(AdmissionRepository admissionRepository,
                            PatientRepository patientRepository,
                            PatientJourneyRepository journeyRepository,
                            MasterTableRepository masterTableRepository,
                            AuditService auditService) {
        this.admissionRepository = admissionRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
        this.masterTableRepository = masterTableRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AdmissionResponse save(Long patientId, AdmissionRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        if (request.status() != null) {
            masterTableRepository.findByTypeAndLookupCode("PATIENT_STATUS", request.status().toUpperCase())
                    .orElseThrow(() -> new ElderCareException("Invalid status: " + request.status()));
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        AdmissionEntity admission;
        if (request.id() != null) {
            admission = admissionRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Admission", request.id()));
            if (!admission.getPatient().getId().equals(patientId)) {
                throw new ElderCareException("Admission does not belong to this patient");
            }
        } else {
            admission = admissionRepository.findByPatientId(patientId)
                    .orElse(new AdmissionEntity());
        }

        boolean isNew = admission.getId() == null;
        admission.setPatient(patient);
        admission.setAdmissionDate(request.admissionDate());
        admission.setRoomNumber(request.roomNumber());
        admission.setBed(request.bed());
        admission.setStatus(request.status() != null ? request.status().toUpperCase() : null);
        admission.setEmrContactName(request.emrContactName());
        admission.setPhoneNumber(request.phoneNumber());

        if (isNew) admission.setCreatedBy(currentUser);
        else admission.setUpdatedBy(currentUser);

        admissionRepository.save(admission);

        AdmissionResponse response = toResponse(admission);
        auditService.record(patientId, isNew ? "ADMISSION" : "ADMISSION_UPDATE", response);

        PatientJourneyEntity journey = journeyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient journey", patientId));
        if (!journey.isAdmission()) {
            journey.setAdmission(true);
            journey.setUpdatedBy(currentUser);
            journeyRepository.save(journey);
        }

        log.info("Admission saved for patient {} by {}", patientId, currentUser);
        return response;
    }

    public AdmissionResponse getByPatient(Long patientId) {
        return admissionRepository.findByPatientId(patientId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Admission not found for patient " + patientId));
    }

    private AdmissionResponse toResponse(AdmissionEntity a) {
        String statusDisplay = null;
        if (a.getStatus() != null) {
            statusDisplay = masterTableRepository
                    .findByTypeAndLookupCode("PATIENT_STATUS", a.getStatus())
                    .map(m -> m.getLookupItem())
                    .orElse(a.getStatus());
        }
        return new AdmissionResponse(a.getId(), a.getAdmissionDate(), a.getRoomNumber(),
                a.getBed(), a.getStatus(), statusDisplay, a.getEmrContactName(),
                a.getPhoneNumber(), a.getPatient().getId());
    }
}
