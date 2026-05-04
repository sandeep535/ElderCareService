package com.eldercare.service.repository;

import com.eldercare.service.entity.DiagnosisEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisRepository extends JpaRepository<DiagnosisEntity, Long> {

    List<DiagnosisEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);
}
