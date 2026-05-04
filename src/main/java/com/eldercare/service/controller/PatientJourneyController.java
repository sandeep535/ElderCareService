package com.eldercare.service.controller;

import com.eldercare.service.dto.PatientJourneyResponse;
import com.eldercare.service.entity.PatientJourneyEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientJourneyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/journey")
public class PatientJourneyController {

    private final PatientJourneyRepository journeyRepository;

    public PatientJourneyController(PatientJourneyRepository journeyRepository) {
        this.journeyRepository = journeyRepository;
    }

    @GetMapping
    public ResponseEntity<PatientJourneyResponse> get(@PathVariable Long patientId) {
        PatientJourneyEntity journey = journeyRepository.findByPatientId(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient journey", patientId));

        List<String> pending = new ArrayList<>();
        if (!journey.isMedical())   pending.add("MEDICAL");
        if (!journey.isAdmission()) pending.add("ADMISSION");
        if (!journey.isNote())      pending.add("NOTES");

        return ResponseEntity.ok(new PatientJourneyResponse(
                patientId,
                journey.isBasicDetails(),
                journey.isMedical(),
                journey.isAdmission(),
                journey.isNote(),
                pending.isEmpty(),
                pending
        ));
    }
}
