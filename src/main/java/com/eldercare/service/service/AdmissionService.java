package com.eldercare.service.service;

import com.eldercare.service.dto.AdmissionRequest;
import com.eldercare.service.dto.AdmissionResponse;
import com.eldercare.service.entity.AdmissionEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AdmissionRepository;
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
    private final AuditService auditService;

    public AdmissionService(AdmissionRepository admissionRepository,
                            PatientRepository patientRepository,
                            PatientJourneyRepository journeyRepository,
                            AuditService auditService) {
        this.admissionRepository = admissionRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
        this.auditService = auditService;
    }

    @Transactional
    public AdmissionResponse save(Long patientId, AdmissionRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        AdmissionEntity admission = admissionRepository.findByPatientId(patientId)
                .orElse(new AdmissionEntity());

        boolean isNew = admission.getId() == null;
        admission.setPatient(patient);
        admission.setAdmissionDate(request.admissionDate());
        admission.setRoomNumber(request.roomNumber());
        admission.setBed(request.bed());
        admission.setStatus(request.status());
        admission.setEmrContactName(request.emrContactName());
        admission.setPhoneNumber(request.phoneNumber());

        if (isNew) admission.setCreatedBy(currentUser);
        else admission.setUpdatedBy(currentUser);

        admissionRepository.save(admission);

        AdmissionResponse response = toResponse(admission);
        auditService.record(patientId, "ADMISSION", response);

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

    @Transactional
    public AdmissionResponse update(Long patientId, Long admissionId, AdmissionRequest request) {
        AdmissionEntity admission = admissionRepository.findById(admissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Admission", admissionId));

        if (!admission.getPatient().getId().equals(patientId)) {
            throw new ResourceNotFoundException("Admission", admissionId);
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        admission.setAdmissionDate(request.admissionDate());
        admission.setRoomNumber(request.roomNumber());
        admission.setBed(request.bed());
        admission.setStatus(request.status());
        admission.setEmrContactName(request.emrContactName());
        admission.setPhoneNumber(request.phoneNumber());
        admission.setUpdatedBy(currentUser);

        admissionRepository.save(admission);

        AdmissionResponse response = toResponse(admission);
        auditService.record(patientId, "ADMISSION_UPDATE", response);

        log.info("Admission {} updated for patient {} by {}", admissionId, patientId, currentUser);
        return response;
    }

    private AdmissionResponse toResponse(AdmissionEntity a) {
        return new AdmissionResponse(a.getId(), a.getAdmissionDate(), a.getRoomNumber(),
                a.getBed(), a.getStatus(), a.getEmrContactName(),
                a.getPhoneNumber(), a.getPatient().getId());
    }
}
