package com.eldercare.service.controller;

import com.eldercare.service.dto.VitalMetricBulkUpdateRequest;
import com.eldercare.service.dto.VitalMetricMasterRequest;
import com.eldercare.service.dto.VitalMetricMasterResponse;
import com.eldercare.service.service.VitalMetricMasterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vital-metrics")
public class VitalMetricMasterController {

    private final VitalMetricMasterService service;

    public VitalMetricMasterController(VitalMetricMasterService service) {
        this.service = service;
    }

    // GET all active metrics (admin/config screen — shows all 22)
    @GetMapping
    public ResponseEntity<List<VitalMetricMasterResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    // GET only metrics marked display=true (vitals capture/display screen)
    @GetMapping("/display")
    public ResponseEntity<List<VitalMetricMasterResponse>> getDisplayMetrics() {
        return ResponseEntity.ok(service.getDisplayMetrics());
    }

    // GET single metric by ID
    @GetMapping("/{id}")
    public ResponseEntity<VitalMetricMasterResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    // POST — create new metric (ADMIN, NURSE, DOCTOR)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<VitalMetricMasterResponse> create(
            @Valid @RequestBody VitalMetricMasterRequest request) {
        return ResponseEntity.ok(service.create(request));
    }

    // PUT — update existing metric (ADMIN, NURSE, DOCTOR)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<VitalMetricMasterResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody VitalMetricMasterRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    // PUT — bulk update all metrics at once (ADMIN, NURSE, DOCTOR)
    @PutMapping("/bulk")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<List<VitalMetricMasterResponse>> bulkUpdate(
            @Valid @RequestBody List<VitalMetricBulkUpdateRequest> requests) {
        return ResponseEntity.ok(service.bulkUpdate(requests));
    }
}
