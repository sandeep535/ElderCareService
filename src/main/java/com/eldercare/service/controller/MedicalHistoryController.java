package com.eldercare.service.controller;

import com.eldercare.service.dto.MedicalHistoryRequest;
import com.eldercare.service.dto.MedicalHistoryResponse;
import com.eldercare.service.service.MedicalHistoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/medical-history")
public class MedicalHistoryController {

    private final MedicalHistoryService medicalHistoryService;

    public MedicalHistoryController(MedicalHistoryService medicalHistoryService) {
        this.medicalHistoryService = medicalHistoryService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<MedicalHistoryResponse> add(@PathVariable Long patientId,
                                                      @Valid @RequestBody MedicalHistoryRequest request) {
        return ResponseEntity.ok(medicalHistoryService.add(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<MedicalHistoryResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicalHistoryService.getByPatient(patientId));
    }

    @PutMapping("/{historyId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<MedicalHistoryResponse> update(@PathVariable Long patientId,
                                                          @PathVariable Long historyId,
                                                          @Valid @RequestBody MedicalHistoryRequest request) {
        return ResponseEntity.ok(medicalHistoryService.update(patientId, historyId, request));
    }
}
