package com.eldercare.service.service;

import com.eldercare.service.dto.PatientCheckinResponse;
import com.eldercare.service.entity.PatientCheckinEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientCheckinRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PatientCheckinService {

    private final PatientCheckinRepository checkinRepository;
    private final PatientRepository patientRepository;

    public PatientCheckinService(PatientCheckinRepository checkinRepository,
                                 PatientRepository patientRepository) {
        this.checkinRepository = checkinRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public PatientCheckinResponse checkIn(Long patientId) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        // Block if an active check-in already exists
        if (checkinRepository.findByPatientIdAndCheckOutTimeIsNull(patientId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Patient already has an active check-in. Please check out first.");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        PatientCheckinEntity entity = new PatientCheckinEntity();
        entity.setPatient(patient);
        entity.setCheckInTime(LocalDateTime.now());
        entity.setCreatedBy(currentUser);

        checkinRepository.save(entity);
        return toResponse(entity);
    }

    public Optional<PatientCheckinResponse> getActive(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }

        // Return active check-in if exists
        Optional<PatientCheckinEntity> active =
                checkinRepository.findByPatientIdAndCheckOutTimeIsNull(patientId);
        if (active.isPresent()) {
            return active.map(this::toResponse);
        }

        // No active check-in — return last completed visit so UI can show summary
        return checkinRepository
                .findTopByPatientIdAndCheckOutTimeIsNotNullOrderByCheckOutTimeDesc(patientId)
                .map(this::toResponse);
    }

    @Transactional
    public PatientCheckinResponse checkOut(Long patientId, Long checkinId) {
        PatientCheckinEntity entity = checkinRepository.findById(checkinId)
                .orElseThrow(() -> new ResourceNotFoundException("Check-in record", checkinId));

        if (!entity.getPatient().getId().equals(patientId)) {
            throw new ElderCareException("Check-in record does not belong to this patient");
        }

        if (entity.getCheckOutTime() != null) {
            throw new ElderCareException("Patient has already been checked out");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setCheckOutTime(LocalDateTime.now());
        entity.setUpdatedBy(currentUser);

        checkinRepository.save(entity);
        return toResponse(entity);
    }

    public List<PatientCheckinResponse> getHistory(Long patientId, LocalDate from, LocalDate to) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return checkinRepository.findByPatientIdAndCheckInTimeBetweenOrderByCheckInTimeDesc(
                        patientId,
                        from.atStartOfDay(),
                        to.atTime(23, 59, 59))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PatientCheckinResponse toResponse(PatientCheckinEntity e) {
        return new PatientCheckinResponse(
                e.getId(),
                e.getPatient().getId(),
                e.getCheckInTime(),
                e.getCheckOutTime(),
                e.getCreatedBy(),
                e.getCreatedOn()
        );
    }
}
