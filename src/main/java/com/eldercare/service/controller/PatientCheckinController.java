package com.eldercare.service.controller;

import com.eldercare.service.dto.PatientCheckinResponse;
import com.eldercare.service.service.PatientCheckinService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/patients/{patientId}/checkin")
public class PatientCheckinController {

    private final PatientCheckinService checkinService;

    public PatientCheckinController(PatientCheckinService checkinService) {
        this.checkinService = checkinService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<PatientCheckinResponse> checkIn(@PathVariable Long patientId) {
        return ResponseEntity.ok(checkinService.checkIn(patientId));
    }

    @GetMapping("/active")
    public ResponseEntity<PatientCheckinResponse> getActive(@PathVariable Long patientId) {
        Optional<PatientCheckinResponse> result = checkinService.getActive(patientId);
        return result.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PutMapping("/{checkinId}/checkout")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<PatientCheckinResponse> checkOut(@PathVariable Long patientId,
                                                           @PathVariable Long checkinId) {
        return ResponseEntity.ok(checkinService.checkOut(patientId, checkinId));
    }

    @GetMapping("/history")
    public ResponseEntity<List<PatientCheckinResponse>> getHistory(
            @PathVariable Long patientId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(checkinService.getHistory(patientId, from, to));
    }
}
