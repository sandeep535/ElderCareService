package com.eldercare.service.repository;

import com.eldercare.service.entity.AlertEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertRepository extends JpaRepository<AlertEntity, Long> {

    List<AlertEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);

    List<AlertEntity> findByPatientIdAndResolvedFalseOrderByCreatedOnDesc(Long patientId);

    Page<AlertEntity> findByPatientId(Long patientId, Pageable pageable);

    Page<AlertEntity> findAll(Pageable pageable);

    Page<AlertEntity> findByResolved(boolean resolved, Pageable pageable);

    long countByResolvedFalse();
}
