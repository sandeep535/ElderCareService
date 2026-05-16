package com.eldercare.service.service;

import com.eldercare.service.dto.VitalMetricBulkUpdateRequest;
import com.eldercare.service.dto.VitalMetricMasterRequest;
import com.eldercare.service.dto.VitalMetricMasterResponse;
import com.eldercare.service.entity.VitalMetricMasterEntity;
import com.eldercare.service.exception.ElderCareException;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.VitalMetricMasterRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VitalMetricMasterService {

    private static final Logger log = LogManager.getLogger(VitalMetricMasterService.class);

    private final VitalMetricMasterRepository repository;

    public VitalMetricMasterService(VitalMetricMasterRepository repository) {
        this.repository = repository;
    }

    // ── CREATE ────────────────────────────────────────────────────────────────

    @Transactional
    public VitalMetricMasterResponse create(VitalMetricMasterRequest request) {
        if (repository.existsByDeviceId(request.deviceId())) {
            throw new ElderCareException("Device ID " + request.deviceId() + " already exists");
        }
        if (repository.existsByFieldKey(request.fieldKey())) {
            throw new ElderCareException("Field key '" + request.fieldKey() + "' already exists");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        VitalMetricMasterEntity entity = new VitalMetricMasterEntity();
        mapRequestToEntity(request, entity);
        entity.setCreatedBy(currentUser);

        repository.save(entity);
        log.info("Vital metric created: deviceId={} fieldKey={} by {}", request.deviceId(), request.fieldKey(), currentUser);
        return toResponse(entity);
    }

    // ── UPDATE ────────────────────────────────────────────────────────────────

    @Transactional
    public VitalMetricMasterResponse update(Long id, VitalMetricMasterRequest request) {
        VitalMetricMasterEntity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vital metric", id));

        // Check device ID uniqueness (excluding self)
        if (!entity.getDeviceId().equals(request.deviceId()) &&
                repository.existsByDeviceId(request.deviceId())) {
            throw new ElderCareException("Device ID " + request.deviceId() + " already exists");
        }

        // Check field key uniqueness (excluding self)
        if (!entity.getFieldKey().equals(request.fieldKey()) &&
                repository.existsByFieldKey(request.fieldKey())) {
            throw new ElderCareException("Field key '" + request.fieldKey() + "' already exists");
        }

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
        mapRequestToEntity(request, entity);
        entity.setUpdatedBy(currentUser);

        repository.save(entity);
        log.info("Vital metric updated: id={} by {}", id, currentUser);
        return toResponse(entity);
    }

    // ── BULK UPDATE ───────────────────────────────────────────────────────────

    @Transactional
    public List<VitalMetricMasterResponse> bulkUpdate(List<VitalMetricBulkUpdateRequest> requests) {
        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        List<VitalMetricMasterEntity> updated = requests.stream().map(req -> {
            VitalMetricMasterEntity entity = repository.findById(req.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Vital metric", req.id()));

            // Only update fields that are provided (non-null)
            if (req.deviceId()    != null) entity.setDeviceId(req.deviceId());
            if (req.fieldKey()    != null) entity.setFieldKey(req.fieldKey().trim().toLowerCase());
            if (req.displayName() != null) entity.setDisplayName(req.displayName());
            if (req.unit()        != null) entity.setUnit(req.unit());
            if (req.category()    != null) entity.setCategory(req.category().toUpperCase());
            if (req.mandatory()   != null) entity.setMandatory(req.mandatory());
            if (req.display()     != null) entity.setDisplay(req.display());
            if (req.lowValue()    != null) entity.setLowValue(req.lowValue());
            if (req.highValue()   != null) entity.setHighValue(req.highValue());
            if (req.normalRange() != null) entity.setNormalRange(req.normalRange());
            if (req.sortOrder()   != null) entity.setSortOrder(req.sortOrder());
            if (req.active()      != null) entity.setActive(req.active());

            entity.setUpdatedBy(currentUser);
            return entity;
        }).toList();

        repository.saveAll(updated);
        log.info("Bulk vital metrics updated: {} records by {}", updated.size(), currentUser);
        return updated.stream().map(this::toResponse).toList();
    }

    // ── GET ALL ───────────────────────────────────────────────────────────────

    public List<VitalMetricMasterResponse> getAll() {
        return repository.findByActiveTrueOrderBySortOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    // ── GET DISPLAY ONLY (for vitals screen) ─────────────────────────────────

    public List<VitalMetricMasterResponse> getDisplayMetrics() {
        return repository.findByActiveTrueAndDisplayTrueOrderBySortOrderAsc()
                .stream().map(this::toResponse).toList();
    }

    // ── GET BY ID ─────────────────────────────────────────────────────────────

    public VitalMetricMasterResponse getById(Long id) {
        return toResponse(repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vital metric", id)));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────────

    private void mapRequestToEntity(VitalMetricMasterRequest r, VitalMetricMasterEntity e) {
        e.setDeviceId(r.deviceId());
        e.setFieldKey(r.fieldKey().trim().toLowerCase());
        e.setDisplayName(r.displayName());
        e.setUnit(r.unit());
        e.setCategory(r.category() != null ? r.category().toUpperCase() : null);
        e.setMandatory(r.mandatory() != null && r.mandatory());
        e.setDisplay(r.display() == null || r.display());
        e.setLowValue(r.lowValue());
        e.setHighValue(r.highValue());
        e.setNormalRange(r.normalRange());
        e.setSortOrder(r.sortOrder() != null ? r.sortOrder() : r.deviceId());
        e.setActive(r.active() == null || r.active());
    }

    public VitalMetricMasterResponse toResponse(VitalMetricMasterEntity e) {
        return new VitalMetricMasterResponse(
                e.getId(),
                e.getDeviceId(),
                e.getFieldKey(),
                e.getDisplayName(),
                e.getUnit(),
                e.getCategory(),
                e.isMandatory(),
                e.isDisplay(),
                e.getLowValue(),
                e.getHighValue(),
                e.getNormalRange(),
                e.getSortOrder(),
                e.isActive()
        );
    }
}
