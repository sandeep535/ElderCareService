package com.eldercare.service.controller;

import com.eldercare.service.dto.MedicationMasterRequest;
import com.eldercare.service.dto.MedicationMasterResponse;
import com.eldercare.service.dto.MedicationSlotResponse;
import com.eldercare.service.dto.PatientMedicationRequest;
import com.eldercare.service.dto.PatientMedicationResponse;
import com.eldercare.service.dto.SlotActionRequest;
import com.eldercare.service.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping("/medication-master")
    @PreAuthorize("hasAnyRole('ADMIN', 'NURSE', 'DOCTOR')")
    public ResponseEntity<List<MedicationMasterResponse>> getMedicationCatalog(
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(medicationService.getMedicationCatalog(search));
    }

    @PostMapping("/medication-master")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicationMasterResponse> createMedicationMaster(
            @Valid @RequestBody MedicationMasterRequest request) {
        return ResponseEntity.ok(medicationService.createMedicationMaster(request));
    }

    @PutMapping("/medication-master/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MedicationMasterResponse> updateMedicationMaster(@PathVariable Long id,
                                                                           @Valid @RequestBody MedicationMasterRequest request) {
        return ResponseEntity.ok(medicationService.updateMedicationMaster(id, request));
    }

    @PostMapping("/patients/{patientId}/medications")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<PatientMedicationResponse> prescribeMedication(@PathVariable Long patientId,
                                                                        @Valid @RequestBody PatientMedicationRequest request) {
        return ResponseEntity.ok(medicationService.prescribe(patientId, request));
    }

    @GetMapping("/patients/{patientId}/medications")
    public ResponseEntity<List<PatientMedicationResponse>> getPrescriptions(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicationService.getActivePrescriptions(patientId));
    }

    @PutMapping("/patients/{patientId}/medications/{prescriptionId}")
    @PreAuthorize("hasAnyRole('NURSE', 'DOCTOR')")
    public ResponseEntity<PatientMedicationResponse> updatePrescription(@PathVariable Long patientId,
                                                                        @PathVariable Long prescriptionId,
                                                                        @Valid @RequestBody PatientMedicationRequest request) {
        return ResponseEntity.ok(medicationService.updatePrescription(patientId, prescriptionId, request));
    }

    @PutMapping("/patients/{patientId}/medications/{prescriptionId}/stop")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public ResponseEntity<PatientMedicationResponse> stopPrescription(@PathVariable Long patientId,
                                                                      @PathVariable Long prescriptionId) {
        return ResponseEntity.ok(medicationService.stopPrescription(patientId, prescriptionId));
    }

    @GetMapping("/patients/{patientId}/medications/next-due")
    public ResponseEntity<List<MedicationSlotResponse>> getNextDueSlots(@PathVariable Long patientId) {
        return ResponseEntity.ok(medicationService.getNextDueSlots(patientId));
    }

    @GetMapping("/patients/{patientId}/medications/slots")
    public ResponseEntity<List<MedicationSlotResponse>> getMedicationSlots(@PathVariable Long patientId,
                                                                           @RequestParam(required = false) LocalDate date) {
        return ResponseEntity.ok(medicationService.getSlots(patientId, date));
    }

    @PutMapping("/patients/{patientId}/medications/slots/{slotId}/given")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public ResponseEntity<MedicationSlotResponse> markSlotGiven(@PathVariable Long patientId,
                                                                @PathVariable Long slotId,
                                                                @RequestBody SlotActionRequest request) {
        return ResponseEntity.ok(medicationService.markSlotGiven(patientId, slotId, request));
    }

    @PutMapping("/patients/{patientId}/medications/slots/{slotId}/skipped")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public ResponseEntity<MedicationSlotResponse> markSlotSkipped(@PathVariable Long patientId,
                                                                  @PathVariable Long slotId,
                                                                  @RequestBody SlotActionRequest request) {
        return ResponseEntity.ok(medicationService.markSlotSkipped(patientId, slotId, request));
    }
}
