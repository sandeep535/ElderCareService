package com.eldercare.service.controller;

import com.eldercare.service.repository.AlertRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PatientRepository patientRepository;
    private final AlertRepository alertRepository;

    public DashboardController(PatientRepository patientRepository, AlertRepository alertRepository) {
        this.patientRepository = patientRepository;
        this.alertRepository = alertRepository;
    }

    @GetMapping("/patient-count")
    public ResponseEntity<Map<String, Long>> getPatientCount() {
        return ResponseEntity.ok(Map.of("totalPatients", patientRepository.count()));
    }

    @GetMapping("/alert-count")
    public ResponseEntity<Map<String, Long>> getUnresolvedAlertCount() {
        return ResponseEntity.ok(Map.of("unresolvedAlerts", alertRepository.countByResolvedFalse()));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalPatients", patientRepository.count(),
                "unresolvedAlerts", alertRepository.countByResolvedFalse()
        ));
    }
}
