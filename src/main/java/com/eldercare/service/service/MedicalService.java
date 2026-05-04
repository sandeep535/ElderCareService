package com.eldercare.service.service;

import com.eldercare.service.dto.MedicalRequest;
import com.eldercare.service.dto.MedicalResponse;
import com.eldercare.service.entity.MedicalEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MedicalRepository;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MedicalService {

    private static final Logger log = LogManager.getLogger(MedicalService.class);

    private final MedicalRepository medicalRepository;
    private final PatientRepository patientRepository;
    private final PatientJourneyRepository journeyRepository;
    private final AuditService auditService;

    public MedicalService(MedicalRepository medicalRepository,
                          PatientRepository patientRepository,
                          PatientJourneyRepository journeyRepository,
                          AuditService auditService) {
        this.medicalRepository = medicalRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MedicalResponse save(Long patientId, MedicalRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        MedicalEntity medical = medicalRepository.findByPatientId(patientId)
                .orElse(new MedicalEntity());

        boolean isNew = medical.getId() == null;
        medical.setPatient(patient);
        medical.setPrimaryPhysician(request.primaryPhysician());
        medical.setNurse(request.nurse());
        medical.setAllergies(request.allergies());
        medical.setMedicalAlerts(request.medicalAlerts());
        medical.setCurrentMedication(request.currentMedication());
        medical.setBloodType(request.bloodType());

        if (isNew) medical.setCreatedBy(currentUser);
        else medical.setUpdatedBy(currentUser);

        medicalRepository.save(medical);

        MedicalResponse response = toResponse(medical);
        auditService.record(patientId, "MEDICAL", response);

        PatientJourneyEntity journey = journeyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient journey", patientId));
        if (!journey.isMedical()) {
            journey.setMedical(true);
            journey.setUpdatedBy(currentUser);
            journeyRepository.save(journey);
        }

        log.info("Medical saved for patient {} by {}", patientId, currentUser);
        return response;
    }

    public MedicalResponse getByPatient(Long patientId) {
        return medicalRepository.findByPatientId(patientId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Medical record not found for patient " + patientId));
    }

    private MedicalResponse toResponse(MedicalEntity m) {
        return new MedicalResponse(m.getId(), m.getPrimaryPhysician(), m.getNurse(),
                m.getAllergies(), m.getMedicalAlerts(), m.getCurrentMedication(),
                m.getBloodType(), m.getPatient().getId());
    }
}
