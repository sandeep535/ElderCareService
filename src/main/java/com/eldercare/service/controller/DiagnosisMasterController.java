package com.eldercare.service.controller;

import com.eldercare.service.dto.DiagnosisMasterRequest;
import com.eldercare.service.dto.DiagnosisMasterResponse;
import com.eldercare.service.service.DiagnosisMasterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diagnosis-master")
public class DiagnosisMasterController {

    private final DiagnosisMasterService diagnosisMasterService;

    public DiagnosisMasterController(DiagnosisMasterService diagnosisMasterService) {
        this.diagnosisMasterService = diagnosisMasterService;
    }

    @GetMapping
    public ResponseEntity<List<DiagnosisMasterResponse>> search(@RequestParam(required = false) String search) {
        return ResponseEntity.ok(diagnosisMasterService.search(search));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiagnosisMasterResponse> create(@Valid @RequestBody DiagnosisMasterRequest request) {
        return ResponseEntity.ok(diagnosisMasterService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiagnosisMasterResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody DiagnosisMasterRequest request) {
        return ResponseEntity.ok(diagnosisMasterService.update(id, request));
    }
}
