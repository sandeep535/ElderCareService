package com.eldercare.service.repository;

import com.eldercare.service.entity.ClinicalNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNoteEntity, Long> {

    List<ClinicalNoteEntity> findByPatientIdOrderByCreatedOnDesc(Long patientId);
}
