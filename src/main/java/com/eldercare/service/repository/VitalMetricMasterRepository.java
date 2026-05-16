package com.eldercare.service.repository;

import com.eldercare.service.entity.VitalMetricMasterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VitalMetricMasterRepository extends JpaRepository<VitalMetricMasterEntity, Long> {

    // All active metrics ordered by sort order
    List<VitalMetricMasterEntity> findByActiveTrueOrderBySortOrderAsc();

    // Only metrics marked for display (for vitals screen)
    List<VitalMetricMasterEntity> findByActiveTrueAndDisplayTrueOrderBySortOrderAsc();

    // Only mandatory metrics
    List<VitalMetricMasterEntity> findByActiveTrueAndMandatoryTrueOrderBySortOrderAsc();

    boolean existsByDeviceId(Integer deviceId);

    boolean existsByFieldKey(String fieldKey);

    Optional<VitalMetricMasterEntity> findByDeviceId(Integer deviceId);

    Optional<VitalMetricMasterEntity> findByFieldKey(String fieldKey);
}
