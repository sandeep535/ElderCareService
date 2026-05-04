package com.eldercare.service.service;

import com.eldercare.service.dto.MasterRequest;
import com.eldercare.service.dto.MasterResponse;
import com.eldercare.service.entity.MasterTableEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.MasterTableRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MasterService {

    private static final Logger log = LogManager.getLogger(MasterService.class);

    private final MasterTableRepository masterTableRepository;

    public MasterService(MasterTableRepository masterTableRepository) {
        this.masterTableRepository = masterTableRepository;
    }

    public List<MasterResponse> getByType(String type) {
        return masterTableRepository.findByTypeOrderByLookupValue(type.toUpperCase())
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public MasterResponse create(MasterRequest request) {
        String type = request.type().toUpperCase();
        String code = request.lookupCode().toUpperCase();

        if (masterTableRepository.existsByTypeAndLookupCode(type, code)) {
            throw new ElderCareException("Lookup code " + code + " already exists for type " + type);
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        MasterTableEntity entity = new MasterTableEntity();
        entity.setType(type);
        entity.setLookupCode(code);
        entity.setLookupItem(request.lookupItem());
        entity.setLookupValue(request.lookupValue());
        entity.setActive(true);
        entity.setCreatedBy(currentUser);
        masterTableRepository.save(entity);

        log.info("Master entry created: type={} code={} by {}", type, code, currentUser);
        return toResponse(entity);
    }

    @Transactional
    public MasterResponse update(Long id, MasterRequest request) {
        MasterTableEntity entity = masterTableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Master entry", id));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        entity.setLookupValue(request.lookupValue());
        entity.setLookupItem(request.lookupItem());
        entity.setUpdatedBy(currentUser);
        masterTableRepository.save(entity);

        return toResponse(entity);
    }

    private MasterResponse toResponse(MasterTableEntity m) {
        return new MasterResponse(m.getId(), m.getLookupValue(),
                m.getLookupItem(), m.getLookupCode(), m.getType());
    }
}
