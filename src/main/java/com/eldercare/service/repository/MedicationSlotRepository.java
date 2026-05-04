package com.eldercare.service.repository;

import com.eldercare.service.entity.MedicationSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicationSlotRepository extends JpaRepository<MedicationSlotEntity, Long> {

    List<MedicationSlotEntity> findByPatientIdAndStatusAndScheduledTimeLessThanEqualOrderByScheduledTimeAsc(
            Long patientId, String status, LocalDateTime scheduledTime);

    List<MedicationSlotEntity> findByPatientIdAndScheduledTimeBetweenOrderByScheduledTimeAsc(
            Long patientId, LocalDateTime start, LocalDateTime end);

    List<MedicationSlotEntity> findByPatientIdOrderByScheduledTimeAsc(Long patientId);

    List<MedicationSlotEntity> findByPatientMedicationIdOrderByScheduledTimeAsc(Long patientMedicationId);

    List<MedicationSlotEntity> findByStatusAndScheduledTimeBefore(String status, LocalDateTime time);
}
