package com.eldercare.service.controller;

import com.eldercare.service.dto.NotesRequest;
import com.eldercare.service.dto.NotesResponse;
import com.eldercare.service.service.NotesService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients/{patientId}/notes")
public class NotesController {

    private final NotesService notesService;

    public NotesController(NotesService notesService) {
        this.notesService = notesService;
    }

    @PostMapping
    public ResponseEntity<NotesResponse> add(@PathVariable Long patientId,
                                             @Valid @RequestBody NotesRequest request) {
        return ResponseEntity.ok(notesService.add(patientId, request));
    }

    @GetMapping
    public ResponseEntity<List<NotesResponse>> getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(notesService.getByPatient(patientId));
    }
}
