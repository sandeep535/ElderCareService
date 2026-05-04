package com.eldercare.service.repository;

import com.eldercare.service.entity.VitalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VitalRepository extends JpaRepository<VitalEntity, Long> {

    List<VitalEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);

    Optional<VitalEntity> findFirstByPatientIdOrderByCreatedOnDesc(Long patientId);
}
