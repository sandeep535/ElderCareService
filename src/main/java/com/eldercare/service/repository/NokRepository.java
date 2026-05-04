package com.eldercare.service.repository;

import com.eldercare.service.entity.NokEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NokRepository extends JpaRepository<NokEntity, Long> {

    List<NokEntity> findByPatientId(Long patientId);

    boolean existsByPatientIdAndPrimaryContactTrue(Long patientId);
}
