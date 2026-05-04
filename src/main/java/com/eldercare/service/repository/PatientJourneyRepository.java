package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientJourneyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientJourneyRepository extends JpaRepository<PatientJourneyEntity, Long> {

    Optional<PatientJourneyEntity> findByPatientId(Long patientId);
}
