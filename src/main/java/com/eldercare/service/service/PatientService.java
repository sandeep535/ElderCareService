package com.eldercare.service.service;

import com.eldercare.service.dto.PatientRequest;
import com.eldercare.service.dto.PatientResponse;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientJourneyRepository;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Service
public class PatientService {

    private static final Logger log = LogManager.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientJourneyRepository journeyRepository;

    public PatientService(PatientRepository patientRepository,
                          PatientJourneyRepository journeyRepository) {
        this.patientRepository = patientRepository;
        this.journeyRepository = journeyRepository;
    }

    @Transactional
    public PatientResponse save(PatientRequest request) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        PatientEntity patient = new PatientEntity();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setCreatedBy(currentUser);
        patientRepository.save(patient);

        patient.setPatientId(generatePatientId(patient.getId()));
        patientRepository.save(patient);

        PatientJourneyEntity journey = new PatientJourneyEntity();
        journey.setPatient(patient);
        journey.setBasicDetails(true);
        journey.setCreatedBy(currentUser);
        journeyRepository.save(journey);

        log.info("Patient registered: {} by {}", patient.getPatientId(), currentUser);
        return toResponse(patient, journey);
    }

    public List<PatientResponse> getAll() {
        return patientRepository.findAll().stream()
                .map(p -> {
                    PatientJourneyEntity journey = journeyRepository
                            .findByPatientId(p.getId()).orElse(null);
                    return toResponse(p, journey);
                })
                .toList();
    }

    public PatientResponse getById(Long id) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));
        PatientJourneyEntity journey = journeyRepository.findByPatientId(id).orElse(null);
        return toResponse(patient, journey);
    }

    @Transactional
    public PatientResponse update(Long id, PatientRequest request) {
        PatientEntity patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", id));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        patient.setFirstName(request.firstName());
        patient.setLastName(request.lastName());
        patient.setDob(request.dob());
        patient.setGender(request.gender());
        patient.setUpdatedBy(currentUser);
        patientRepository.save(patient);

        PatientJourneyEntity journey = journeyRepository.findByPatientId(id).orElse(null);
        log.info("Patient updated: {} by {}", patient.getPatientId(), currentUser);
        return toResponse(patient, journey);
    }

    // EC-2025-0001 format
    private String generatePatientId(Long id) {
        return String.format("EC-%d-%04d", Year.now().getValue(), id);
    }

    private PatientResponse toResponse(PatientEntity p, PatientJourneyEntity journey) {
        List<String> pending = new ArrayList<>();
        boolean isComplete = false;

        if (journey != null) {
            if (!journey.isMedical())   pending.add("MEDICAL");
            if (!journey.isAdmission()) pending.add("ADMISSION");
            if (!journey.isNote())      pending.add("NOTES");
            isComplete = pending.isEmpty();
        }

        return new PatientResponse(
                p.getId(),
                p.getPatientId(),
                p.getFirstName(),
                p.getLastName(),
                p.getDob(),
                p.getGender(),
                isComplete ? "COMPLETE" : "INCOMPLETE",
                pending
        );
    }
}
