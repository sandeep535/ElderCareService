package com.eldercare.service.controller;

import com.eldercare.service.dto.ClinicalNoteRequest;
import com.eldercare.service.dto.ClinicalNoteResponse;
import com.eldercare.service.service.ClinicalNoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/clinical-notes")
public class ClinicalNoteController {

    private final ClinicalNoteService clinicalNoteService;

    public ClinicalNoteController(ClinicalNoteService clinicalNoteService) {
        this.clinicalNoteService = clinicalNoteService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<ClinicalNoteResponse> add(@PathVariable Long patientId,
                                                     @Valid @RequestBody ClinicalNoteRequest request) {
        return ResponseEntity.ok(clinicalNoteService.add(patientId, request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<List<ClinicalNoteResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(clinicalNoteService.getByPatient(patientId));
    }

    @PutMapping("/{noteId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<ClinicalNoteResponse> update(@PathVariable Long patientId,
                                                        @PathVariable Long noteId,
                                                        @Valid @RequestBody ClinicalNoteRequest request) {
        return ResponseEntity.ok(clinicalNoteService.update(patientId, noteId, request));
    }
}
