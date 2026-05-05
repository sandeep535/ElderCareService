package com.eldercare.service.service;

import com.eldercare.service.dto.DiagnosisMasterRequest;
import com.eldercare.service.dto.DiagnosisMasterResponse;
import com.eldercare.service.entity.DiagnosisMasterEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.DiagnosisMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DiagnosisMasterService {

    private final DiagnosisMasterRepository diagnosisMasterRepository;

    public DiagnosisMasterService(DiagnosisMasterRepository diagnosisMasterRepository) {
        this.diagnosisMasterRepository = diagnosisMasterRepository;
    }

    public List<DiagnosisMasterResponse> search(String query) {
        List<DiagnosisMasterEntity> values;
        if (query == null || query.isBlank()) {
            values = diagnosisMasterRepository.findByActiveTrueOrderByDiagnosisNameAsc();
        } else {
            values = diagnosisMasterRepository.findByDiagnosisNameContainingIgnoreCaseAndActiveTrueOrderByDiagnosisNameAsc(query);
        }
        return values.stream().map(this::toResponse).toList();
    }

    @Transactional
    public DiagnosisMasterResponse create(DiagnosisMasterRequest request) {
        DiagnosisMasterEntity entity = new DiagnosisMasterEntity();
        entity.setDiagnosisName(request.diagnosisName());
        entity.setIcdCode(request.icdCode());
        entity.setActive(request.active() == null || request.active());
        diagnosisMasterRepository.save(entity);
        return toResponse(entity);
    }

    @Transactional
    public DiagnosisMasterResponse update(Long id, DiagnosisMasterRequest request) {
        DiagnosisMasterEntity entity = diagnosisMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Diagnosis master", id));
        entity.setDiagnosisName(request.diagnosisName());
        entity.setIcdCode(request.icdCode());
        entity.setActive(request.active() == null || request.active());
        diagnosisMasterRepository.save(entity);
        return toResponse(entity);
    }

    private DiagnosisMasterResponse toResponse(DiagnosisMasterEntity entity) {
        return new DiagnosisMasterResponse(entity.getId(), entity.getDiagnosisName(), entity.getIcdCode(), entity.isActive());
    }
}
