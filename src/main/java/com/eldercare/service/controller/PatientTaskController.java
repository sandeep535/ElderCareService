package com.eldercare.service.controller;

import com.eldercare.service.dto.PatientTaskRequest;
import com.eldercare.service.dto.PatientTaskResponse;
import com.eldercare.service.service.PatientTaskService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/tasks")
public class PatientTaskController {

    private final PatientTaskService patientTaskService;

    public PatientTaskController(PatientTaskService patientTaskService) {
        this.patientTaskService = patientTaskService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<List<PatientTaskResponse>> create(@PathVariable Long patientId,
                                                            @Valid @RequestBody PatientTaskRequest request) {
        return ResponseEntity.ok(patientTaskService.create(patientId, request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<PatientTaskResponse> update(@PathVariable Long patientId,
                                                      @PathVariable Long id,
                                                      @Valid @RequestBody PatientTaskRequest request) {
        return ResponseEntity.ok(patientTaskService.update(patientId, id, request));
    }

    @GetMapping
    public ResponseEntity<List<PatientTaskResponse>> getAll(@PathVariable Long patientId) {
        return ResponseEntity.ok(patientTaskService.getByPatient(patientId));
    }

    @GetMapping("/range")
    public ResponseEntity<List<PatientTaskResponse>> getByDateRange(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(patientTaskService.getByPatientAndDateRange(patientId, from, to));
    }
}
