package com.eldercare.service.controller;

import com.eldercare.service.dto.PatientTaskSummaryResponse;
import com.eldercare.service.repository.AlertRepository;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.PatientTaskRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PatientRepository patientRepository;
    private final AlertRepository alertRepository;
    private final PatientTaskRepository patientTaskRepository;

    public DashboardController(PatientRepository patientRepository,
                               AlertRepository alertRepository,
                               PatientTaskRepository patientTaskRepository) {
        this.patientRepository = patientRepository;
        this.alertRepository = alertRepository;
        this.patientTaskRepository = patientTaskRepository;
    }

    @GetMapping("/patient-count")
    public ResponseEntity<Map<String, Long>> getPatientCount() {
        return ResponseEntity.ok(Map.of("totalPatients", patientRepository.count()));
    }

    @GetMapping("/alert-count")
    public ResponseEntity<Map<String, Long>> getUnresolvedAlertCount() {
        return ResponseEntity.ok(Map.of("unresolvedAlerts", alertRepository.countByResolvedFalse()));
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Long>> getSummary() {
        return ResponseEntity.ok(Map.of(
                "totalPatients", patientRepository.count(),
                "unresolvedAlerts", alertRepository.countByResolvedFalse()
        ));
    }

    /**
     * GET /api/dashboard/tasks/count
     * Optional query param: date (defaults to today)
     * Returns total pending and completed task counts across ALL patients for the given day.
     */
    @GetMapping("/tasks/count")
    public ResponseEntity<Map<String, Object>> getTaskCount(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        long pending = patientTaskRepository.countByStatusAndScheduledDateTimeBetween(
                "PENDING", targetDate.atStartOfDay(), targetDate.atTime(23, 59, 59));

        long completed = patientTaskRepository.countByStatusAndScheduledDateTimeBetween(
                "COMPLETED", targetDate.atStartOfDay(), targetDate.atTime(23, 59, 59));

        return ResponseEntity.ok(Map.of(
                "date", targetDate.toString(),
                "pending", pending,
                "completed", completed,
                "total", pending + completed
        ));
    }

    /**
     * GET /api/dashboard/tasks/summary
     * Optional query param: date (defaults to today)
     * Returns pending and completed task counts per patient for the given day.
     */
    @GetMapping("/tasks/summary")
    public ResponseEntity<List<PatientTaskSummaryResponse>> getTaskSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        LocalDate targetDate = date != null ? date : LocalDate.now();

        List<Object[]> rows = patientTaskRepository.findTaskSummaryPerPatient(
                targetDate.atStartOfDay(),
                targetDate.atTime(23, 59, 59));

        List<PatientTaskSummaryResponse> result = rows.stream()
                .map(row -> new PatientTaskSummaryResponse(
                        ((Number) row[0]).longValue(),   // patientId
                        ((Number) row[1]).longValue(),   // pendingCount
                        ((Number) row[2]).longValue(),   // completedCount
                        ((Number) row[3]).longValue()    // totalCount
                ))
                .toList();

        return ResponseEntity.ok(result);
    }
}
