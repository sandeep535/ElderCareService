package com.eldercare.service.controller;

import com.eldercare.service.dto.AuditFailureResponse;
import com.eldercare.service.dto.AuditLogResponse;
import com.eldercare.service.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class AuditController {

    private final AuditLogService auditLogService;

    public AuditController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/patients/{patientId}/audit")
    public ResponseEntity<List<AuditLogResponse>> getAuditTrail(
            @PathVariable Long patientId,
            @RequestParam(required = false) String type) {
        return ResponseEntity.ok(auditLogService.getByPatient(patientId, type));
    }

    @GetMapping("/patients/{patientId}/audit/last")
    public ResponseEntity<List<AuditLogResponse>> getLastN(
            @PathVariable Long patientId,
            @RequestParam int count) {
        return ResponseEntity.ok(auditLogService.getLastN(patientId, count));
    }

    @GetMapping("/patients/{patientId}/audit/range")
    public ResponseEntity<List<AuditLogResponse>> getByDateRange(
            @PathVariable Long patientId,
            @RequestParam LocalDate from,
            @RequestParam LocalDate to) {
        return ResponseEntity.ok(auditLogService.getByDateRange(patientId, from, to));
    }

    @GetMapping("/audit/failures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditFailureResponse>> getFailures() {
        return ResponseEntity.ok(auditLogService.getPendingFailures());
    }

    @PutMapping("/audit/failures/{id}/resolve")
    public ResponseEntity<AuditFailureResponse> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.resolveFailure(id));
    }
}
