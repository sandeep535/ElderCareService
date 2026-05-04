package com.eldercare.service.controller;

import com.eldercare.service.dto.NokRequest;
import com.eldercare.service.dto.NokResponse;
import com.eldercare.service.service.NokService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/nok")
public class NokController {

    private final NokService nokService;

    public NokController(NokService nokService) {
        this.nokService = nokService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<NokResponse> add(@PathVariable Long patientId,
                                           @Valid @RequestBody NokRequest request) {
        return ResponseEntity.ok(nokService.add(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<NokResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(nokService.getByPatient(patientId));
    }

    @PutMapping("/{nokId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<NokResponse> update(@PathVariable Long patientId,
                                              @PathVariable Long nokId,
                                              @Valid @RequestBody NokRequest request) {
        return ResponseEntity.ok(nokService.update(patientId, nokId, request));
    }
}
