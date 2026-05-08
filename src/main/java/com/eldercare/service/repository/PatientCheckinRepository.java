package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientCheckinEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PatientCheckinRepository extends JpaRepository<PatientCheckinEntity, Long> {

    // Active check-in = checkOutTime is null
    Optional<PatientCheckinEntity> findByPatientIdAndCheckOutTimeIsNull(Long patientId);

    // Last completed check-in = most recent one with checkOutTime set
    Optional<PatientCheckinEntity> findTopByPatientIdAndCheckOutTimeIsNotNullOrderByCheckOutTimeDesc(Long patientId);

    // History — all records where checkInTime falls within the date range
    List<PatientCheckinEntity> findByPatientIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
            Long patientId, LocalDateTime from, LocalDateTime to);
}
