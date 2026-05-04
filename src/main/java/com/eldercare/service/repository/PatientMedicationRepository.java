package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientMedicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PatientMedicationRepository extends JpaRepository<PatientMedicationEntity, Long> {

    List<PatientMedicationEntity> findByPatientIdAndActiveTrue(Long patientId);
}
