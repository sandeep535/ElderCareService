package com.eldercare.service.repository;

import com.eldercare.service.entity.NotesEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotesRepository extends JpaRepository<NotesEntity, Long> {

    List<NotesEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);

    boolean existsByPatientId(Long patientId);
}
