package com.eldercare.service.controller;

import com.eldercare.service.dto.DiagnosisRequest;
import com.eldercare.service.dto.DiagnosisResponse;
import com.eldercare.service.service.DiagnosisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/diagnoses")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public ResponseEntity<DiagnosisResponse> add(@PathVariable Long patientId,
                                                 @Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.ok(diagnosisService.add(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<DiagnosisResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(diagnosisService.getByPatient(patientId));
    }

    @PutMapping("/{diagnosisId}")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public ResponseEntity<DiagnosisResponse> update(@PathVariable Long patientId,
                                                    @PathVariable Long diagnosisId,
                                                    @Valid @RequestBody DiagnosisRequest request) {
        return ResponseEntity.ok(diagnosisService.update(patientId, diagnosisId, request));
    }
}
