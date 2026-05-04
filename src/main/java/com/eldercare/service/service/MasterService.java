package com.eldercare.service.service;

import com.eldercare.service.dto.MasterRequest;
import com.eldercare.service.dto.MasterResponse;
import com.eldercare.service.entity.MasterTableEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MasterService {

    private final MasterTableRepository masterTableRepository;

    public MasterService(MasterTableRepository masterTableRepository) {
        this.masterTableRepository = masterTableRepository;
    }

    public List<MasterResponse> getByType(String type) {
        return masterTableRepository.findByTypeAndActiveTrueOrderByLookupValueAsc(type.toUpperCase())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public MasterResponse create(MasterRequest request) {
        String type = request.type().toUpperCase();
        String lookupCode = request.lookupCode().toUpperCase();

        if (masterTableRepository.existsByTypeAndLookupCode(type, lookupCode)) {
            throw new ElderCareException("Lookup code already exists for type: " + lookupCode);
        }

        MasterTableEntity master = new MasterTableEntity();
        master.setType(type);
        master.setLookupCode(lookupCode);
        master.setLookupItem(request.lookupItem());
        master.setLookupValue(request.lookupValue());
        master.setActive(request.active() == null || request.active());
        masterTableRepository.save(master);
        return toResponse(master);
    }

    @Transactional
    public MasterResponse update(Long id, MasterRequest request) {
        MasterTableEntity master = masterTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master lookup", id));

        String type = request.type().toUpperCase();
        String lookupCode = request.lookupCode().toUpperCase();

        if (!master.getType().equals(type) || !master.getLookupCode().equals(lookupCode)) {
            if (masterTableRepository.existsByTypeAndLookupCode(type, lookupCode)) {
                throw new ElderCareException("Lookup code already exists for type: " + lookupCode);
            }
        }

        master.setType(type);
        master.setLookupCode(lookupCode);
        master.setLookupItem(request.lookupItem());
        master.setLookupValue(request.lookupValue());
        master.setActive(request.active() == null || request.active());
        masterTableRepository.save(master);
        return toResponse(master);
    }

    private MasterResponse toResponse(MasterTableEntity entity) {
        return new MasterResponse(entity.getId(), entity.getLookupValue(), entity.getLookupItem(),
                entity.getLookupCode(), entity.getType(), entity.isActive());
    }
}
