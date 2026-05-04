package com.eldercare.service.repository;

import com.eldercare.service.entity.AdmissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionRepository extends JpaRepository<AdmissionEntity, Long> {

    Optional<AdmissionEntity> findByPatientId(Long patientId);
}
