package com.eldercare.service.controller;

import com.eldercare.service.dto.AdmissionRequest;
import com.eldercare.service.dto.AdmissionResponse;
import com.eldercare.service.service.AdmissionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/patients/{patientId}/admission")
public class AdmissionController {

    private final AdmissionService admissionService;

    public AdmissionController(AdmissionService admissionService) {
        this.admissionService = admissionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<AdmissionResponse> save(@PathVariable Long patientId,
                                                  @RequestBody AdmissionRequest request) {
        return ResponseEntity.ok(admissionService.save(patientId, request));
    }

    @GetMapping
    public ResponseEntity<AdmissionResponse> get(@PathVariable Long patientId) {
        return ResponseEntity.ok(admissionService.getByPatient(patientId));
    }

    @PutMapping("/{admissionId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<AdmissionResponse> update(@PathVariable Long patientId,
                                                    @PathVariable Long admissionId,
                                                    @RequestBody AdmissionRequest request) {
        return ResponseEntity.ok(admissionService.update(patientId, admissionId, request));
    }
}
