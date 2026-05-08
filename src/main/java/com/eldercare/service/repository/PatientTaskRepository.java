package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientTaskRepository extends JpaRepository<PatientTaskEntity, Long> {

    List<PatientTaskEntity> findByPatientIdOrderByScheduledDateTimeAsc(Long patientId);

    List<PatientTaskEntity> findByPatientIdAndScheduledDateTimeBetweenOrderByScheduledDateTimeAsc(
            Long patientId, LocalDateTime from, LocalDateTime to);

    // Per-patient task summary for today — returns [patientId, pending, completed, total]
    @Query("""
            SELECT pt.patient.id,
                   SUM(CASE WHEN pt.status = 'PENDING'   THEN 1 ELSE 0 END),
                   SUM(CASE WHEN pt.status = 'COMPLETED' THEN 1 ELSE 0 END),
                   COUNT(pt.id)
            FROM PatientTaskEntity pt
            WHERE pt.scheduledDateTime >= :from
              AND pt.scheduledDateTime <= :to
            GROUP BY pt.patient.id
            """)
    List<Object[]> findTaskSummaryPerPatient(@Param("from") LocalDateTime from,
                                             @Param("to") LocalDateTime to);

    // Total pending count across all patients for a date range
    long countByStatusAndScheduledDateTimeBetween(String status, LocalDateTime from, LocalDateTime to);
}
