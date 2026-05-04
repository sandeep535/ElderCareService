package com.eldercare.service.controller;

import com.eldercare.service.dto.MedicalRequest;
import com.eldercare.service.dto.MedicalResponse;
import com.eldercare.service.service.MedicalService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}/medical")
public class MedicalController {

    private final MedicalService medicalService;

    public MedicalController(MedicalService medicalService) {
        this.medicalService = medicalService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<MedicalResponse> save(@PathVariable Long patientId,
                                                @RequestBody MedicalRequest request) {
        return ResponseEntity.ok(medicalService.save(patientId, request));
    }

    @GetMapping
    public ResponseEntity<MedicalResponse> get(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalService.getByPatient(patientId));
    }
}
