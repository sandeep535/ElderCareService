package com.eldercare.service.repository;

import com.eldercare.service.entity.MedicalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MedicalRepository extends JpaRepository<MedicalEntity, Long> {

    Optional<MedicalEntity> findByPatientId(Long patientId);
}
