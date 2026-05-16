package com.eldercare.service.controller;

import com.eldercare.service.dto.VitalRequest;
import com.eldercare.service.dto.VitalResponse;
import com.eldercare.service.dto.VitalMetricMasterResponse;
import com.eldercare.service.service.VitalService;
import com.eldercare.service.service.VitalMetricMasterService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/vitals")
public class VitalController {

    private final VitalService vitalService;
    private final VitalMetricMasterService vitalMetricMasterService;

    public VitalController(VitalService vitalService, VitalMetricMasterService vitalMetricMasterService) {
        this.vitalService = vitalService;
        this.vitalMetricMasterService = vitalMetricMasterService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<VitalResponse> record(@PathVariable Long patientId,
                                                @RequestBody VitalRequest request) {
        return ResponseEntity.ok(vitalService.record(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<VitalResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(vitalService.getByPatient(patientId));
    }

    @GetMapping("/latest")
    public ResponseEntity<VitalResponse> getLatest(@PathVariable Long patientId) {
        return ResponseEntity.ok(vitalService.getLatest(patientId));
    }

    @GetMapping("/metrics")
    public ResponseEntity<List<VitalMetricMasterResponse>> getActiveMetrics() {
        return ResponseEntity.ok(vitalMetricMasterService.getDisplayMetrics());
    }
}
