package com.eldercare.service.repository;

import com.eldercare.service.entity.MedicalHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistoryEntity, Long> {

    List<MedicalHistoryEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);
}
