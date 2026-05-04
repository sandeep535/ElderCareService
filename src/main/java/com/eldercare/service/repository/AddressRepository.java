package com.eldercare.service.repository;

import com.eldercare.service.entity.AddressEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<AddressEntity, Long> {

    Optional<AddressEntity> findByTypeAndTypeId(String type, Long typeId);

    List<AddressEntity> findAllByTypeAndTypeId(String type, Long typeId);
}
