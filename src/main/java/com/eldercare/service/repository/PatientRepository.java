package com.eldercare.service.repository;

import com.eldercare.service.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

    Optional<PatientEntity> findByPatientId(String patientId);

    boolean existsByPatientId(String patientId);
}
