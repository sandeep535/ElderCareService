package com.eldercare.service.repository;

import com.eldercare.service.entity.DiagnosisMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiagnosisMasterRepository extends JpaRepository<DiagnosisMasterEntity, Long> {

    List<DiagnosisMasterEntity> findByActiveTrueOrderByDiagnosisNameAsc();

    List<DiagnosisMasterEntity> findByDiagnosisNameContainingIgnoreCaseAndActiveTrueOrderByDiagnosisNameAsc(String query);
}
