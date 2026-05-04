package com.eldercare.service.repository;

import com.eldercare.service.entity.MasterTableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MasterTableRepository extends JpaRepository<MasterTableEntity, Long> {

    List<MasterTableEntity> findByTypeAndActiveTrueOrderByLookupValueAsc(String type);

    Optional<MasterTableEntity> findByTypeAndLookupCode(String type, String lookupCode);

    boolean existsByTypeAndLookupCode(String type, String lookupCode);
}
