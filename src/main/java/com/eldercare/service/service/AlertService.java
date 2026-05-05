package com.eldercare.service.service;

import com.eldercare.service.dto.AlertResponse;
import com.eldercare.service.dto.PagedAlertResponse;
import com.eldercare.service.entity.AlertEntity;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.AlertRepository;
import com.eldercare.service.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final PatientRepository patientRepository;

    public AlertService(AlertRepository alertRepository, PatientRepository patientRepository) {
        this.alertRepository = alertRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public void saveAlert(Long patientId, String typeOfScreen, String name,
                          String value, String reason, String priority, String createdBy) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        AlertEntity alert = new AlertEntity();
        alert.setPatient(patient);
        alert.setTypeOfScreen(typeOfScreen);
        alert.setName(name);
        alert.setValue(value);
        alert.setReason(reason);
        alert.setPriority(priority);
        alert.setResolved(false);
        alert.setCreatedBy(createdBy);
        alertRepository.save(alert);
    }

    public List<AlertResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return alertRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public List<AlertResponse> getUnresolvedByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return alertRepository.findByPatientIdAndResolvedFalseOrderByCreatedOnDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public PagedAlertResponse getAllPaged(int page, int size) {
        Page<AlertEntity> result = alertRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
        return new PagedAlertResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    public PagedAlertResponse getByPatientPaged(Long patientId, int page, int size) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        Page<AlertEntity> result = alertRepository.findByPatientId(
                patientId, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdOn")));
        return new PagedAlertResponse(
                result.getContent().stream().map(this::toResponse).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isLast()
        );
    }

    private AlertResponse toResponse(AlertEntity a) {
        PatientEntity p = a.getPatient();
        return new AlertResponse(
                a.getId(),
                p.getId(),
                p.getPatientId(),
                p.getFirstName() + " " + p.getLastName(),
                a.getTypeOfScreen(), a.getName(), a.getValue(),
                a.getReason(), a.getPriority(), a.isResolved(),
                a.getCreatedBy(), a.getCreatedOn(),
                a.getUpdatedBy(), a.getUpdatedOn()
        );
    }
}
