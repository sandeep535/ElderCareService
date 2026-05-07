package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PatientTaskRepository extends JpaRepository<PatientTaskEntity, Long> {

    List<PatientTaskEntity> findByPatientIdOrderByScheduledDateTimeAsc(Long patientId);

    List<PatientTaskEntity> findByPatientIdAndScheduledDateTimeBetweenOrderByScheduledDateTimeAsc(
            Long patientId, LocalDateTime from, LocalDateTime to);
}
