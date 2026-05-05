package com.eldercare.service.controller;

import com.eldercare.service.dto.AlertResponse;
import com.eldercare.service.dto.PagedAlertResponse;
import com.eldercare.service.service.AlertService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping("/api/patients/{patientId}/alerts")
    public ResponseEntity<List<AlertResponse>> getAll(@PathVariable Long patientId) {
        return ResponseEntity.ok(alertService.getByPatient(patientId));
    }

    @GetMapping("/api/patients/{patientId}/alerts/unresolved")
    public ResponseEntity<List<AlertResponse>> getUnresolved(@PathVariable Long patientId) {
        return ResponseEntity.ok(alertService.getUnresolvedByPatient(patientId));
    }

    @GetMapping("/api/patients/{patientId}/alerts/paged")
    public ResponseEntity<PagedAlertResponse> getPaged(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alertService.getByPatientPaged(patientId, page, size));
    }

    @GetMapping("/api/alerts/paged")
    public ResponseEntity<PagedAlertResponse> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(alertService.getAllPaged(page, size));
    }
}
