package com.eldercare.service.service;

import com.eldercare.service.dto.MedicationMasterRequest;
import com.eldercare.service.dto.MedicationMasterResponse;
import com.eldercare.service.entity.MedicationMasterEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MedicationMasterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MedicationMasterService {

    private final MedicationMasterRepository medicationMasterRepository;

    public MedicationMasterService(MedicationMasterRepository medicationMasterRepository) {
        this.medicationMasterRepository = medicationMasterRepository;
    }

    @Transactional
    public MedicationMasterResponse create(MedicationMasterRequest request) {
        if (medicationMasterRepository.existsByDrugNameIgnoreCase(request.drugName())) {
            throw new ElderCareException("Drug already exists: " + request.drugName());
        }
        MedicationMasterEntity entity = new MedicationMasterEntity();
        mapFields(entity, request);
        entity.setActive(request.active() == null || request.active());
        medicationMasterRepository.save(entity);
        return toResponse(entity);
    }

    public List<MedicationMasterResponse> getAll(String search) {
        if (search == null || search.isBlank()) {
            return medicationMasterRepository.findByActiveTrueOrderByDrugNameAsc()
                    .stream().map(this::toResponse).toList();
        }
        return medicationMasterRepository
                .findByDrugNameContainingIgnoreCaseAndActiveTrueOrderByDrugNameAsc(search)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MedicationMasterResponse update(Long id, MedicationMasterRequest request) {
        MedicationMasterEntity entity = medicationMasterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medication", id));

        if (!entity.getDrugName().equalsIgnoreCase(request.drugName()) &&
                medicationMasterRepository.existsByDrugNameIgnoreCase(request.drugName())) {
            throw new ElderCareException("Drug already exists: " + request.drugName());
        }

        mapFields(entity, request);
        if (request.active() != null) entity.setActive(request.active());
        medicationMasterRepository.save(entity);
        return toResponse(entity);
    }

    private void mapFields(MedicationMasterEntity entity, MedicationMasterRequest request) {
        entity.setDrugName(request.drugName());
        entity.setGenericName(request.genericName());
        entity.setDefaultStrength(request.defaultStrength());
        entity.setDefaultStrengthUnit(request.defaultStrengthUnit());
        entity.setDefaultDoseForm(request.defaultDoseForm());
        entity.setManufacturer(request.manufacturer());
    }

    private MedicationMasterResponse toResponse(MedicationMasterEntity e) {
        return new MedicationMasterResponse(
                e.getId(), e.getDrugName(), e.getGenericName(),
                e.getDefaultStrength(), e.getDefaultStrengthUnit(),
                e.getDefaultDoseForm(), e.getManufacturer(), e.isActive()
        );
    }
}
