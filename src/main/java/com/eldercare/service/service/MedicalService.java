package com.eldercare.service.service;

import com.eldercare.service.dto.MedicalRequest;
import com.eldercare.service.dto.MedicalResponse;
import com.eldercare.service.entity.MedicalEntity;
import com.eldercare.service.entity.MasterTableEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.entity.UserEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import com.eldercare.service.repository.MedicalRepository;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.UserRepository;
import com.eldercare.service.repository.UserDetailsRepository;
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
    private final UserRepository userRepository;
    private final UserDetailsRepository userDetailsRepository;
    private final MasterTableRepository masterTableRepository;
    private final AuditService auditService;

    public MedicalService(MedicalRepository medicalRepository,
                          PatientRepository patientRepository,
                          PatientJourneyRepository journeyRepository,
                          UserRepository userRepository,
                          UserDetailsRepository userDetailsRepository,
                          MasterTableRepository masterTableRepository,
                          AuditService auditService) {
        this.medicalRepository = medicalRepository;
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
        this.userRepository = userRepository;
        this.userDetailsRepository = userDetailsRepository;
        this.masterTableRepository = masterTableRepository;
        this.auditService = auditService;
    }

    @Transactional
    public MedicalResponse save(Long patientId, MedicalRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        UserEntity physician = null;
        if (request.primaryPhysicianId() != null) {
            physician = userRepository.findById(request.primaryPhysicianId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.primaryPhysicianId()));
            if (!"DOCTOR".equalsIgnoreCase(physician.getUserType())) {
                throw new ElderCareException("Primary physician must be a DOCTOR");
            }
        }

        UserEntity nurse = null;
        if (request.nurseId() != null) {
            nurse = userRepository.findById(request.nurseId())
                    .orElseThrow(() -> new ResourceNotFoundException("User", request.nurseId()));
            if (!"NURSE".equalsIgnoreCase(nurse.getUserType())) {
                throw new ElderCareException("Nurse must be a NURSE");
            }
        }

        if (request.bloodType() != null) {
            masterTableRepository.findByTypeAndLookupCode("BLOOD_TYPE", request.bloodType().toUpperCase())
                    .orElseThrow(() -> new ElderCareException("Invalid blood type: " + request.bloodType()));
        }

        MedicalEntity medical;
        if (request.id() != null) {
            medical = medicalRepository.findById(request.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Medical record", request.id()));
            if (!medical.getPatient().getId().equals(patientId)) {
                throw new ElderCareException("Medical record does not belong to this patient");
            }
        } else {
            medical = medicalRepository.findByPatientId(patientId)
                    .orElse(new MedicalEntity());
        }

        boolean isNew = medical.getId() == null;
        medical.setPatient(patient);
        medical.setPrimaryPhysician(physician);
        medical.setNurse(nurse);
        medical.setAllergies(request.allergies());
        medical.setMedicalAlerts(request.medicalAlerts());
        medical.setCurrentMedication(request.currentMedication());
        medical.setBloodType(request.bloodType() != null ? request.bloodType().toUpperCase() : null);

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
        String bloodTypeDisplay = null;
        if (m.getBloodType() != null) {
            bloodTypeDisplay = masterTableRepository
                    .findByTypeAndLookupCode("BLOOD_TYPE", m.getBloodType())
                    .map(MasterTableEntity::getLookupItem)
                    .orElse(m.getBloodType());
        }

        Long physicianId = m.getPrimaryPhysician() != null ? m.getPrimaryPhysician().getId() : null;
        String physicianName = physicianId != null
                ? userDetailsRepository.findByUserId(physicianId)
                        .map(d -> d.getFirstName() + " " + d.getLastName())
                        .orElse(m.getPrimaryPhysician().getUsername())
                : null;

        Long nurseId = m.getNurse() != null ? m.getNurse().getId() : null;
        String nurseName = nurseId != null
                ? userDetailsRepository.findByUserId(nurseId)
                        .map(d -> d.getFirstName() + " " + d.getLastName())
                        .orElse(m.getNurse().getUsername())
                : null;

        return new MedicalResponse(
                m.getId(),
                physicianId,
                physicianName,
                nurseId,
                nurseName,
                m.getAllergies(),
                m.getMedicalAlerts(),
                m.getCurrentMedication(),
                m.getBloodType(),
                bloodTypeDisplay,
                m.getPatient().getId()
        );
    }
}
