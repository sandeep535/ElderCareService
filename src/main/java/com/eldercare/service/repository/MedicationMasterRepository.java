package com.eldercare.service.repository;

import com.eldercare.service.entity.MedicationMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicationMasterRepository extends JpaRepository<MedicationMasterEntity, Long> {

    List<MedicationMasterEntity> findByActiveTrueOrderByNameAsc();
}
