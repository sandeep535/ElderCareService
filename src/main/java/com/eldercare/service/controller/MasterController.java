package com.eldercare.service.controller;

import com.eldercare.service.dto.MasterRequest;
import com.eldercare.service.dto.MasterResponse;
import com.eldercare.service.service.MasterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/masters")
public class MasterController {

    private final MasterService masterService;

    public MasterController(MasterService masterService) {
        this.masterService = masterService;
    }

    @GetMapping("/{type}")
    public ResponseEntity<List<MasterResponse>> getByType(@PathVariable String type) {
        return ResponseEntity.ok(masterService.getByType(type));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MasterResponse> create(@Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(masterService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MasterResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody MasterRequest request) {
        return ResponseEntity.ok(masterService.update(id, request));
    }
}
