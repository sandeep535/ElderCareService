package com.eldercare.service.service;

import com.eldercare.service.config.VitalThresholdConfig;
import com.eldercare.service.dto.VitalRequest;
import com.eldercare.service.dto.VitalResponse;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.VitalEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.VitalRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VitalService {

    private static final Logger log = LogManager.getLogger(VitalService.class);

    private final VitalRepository vitalRepository;
    private final PatientRepository patientRepository;
    private final VitalThresholdConfig thresholds;
    private final AuditService auditService;

    public VitalService(VitalRepository vitalRepository,
                        PatientRepository patientRepository,
                        VitalThresholdConfig thresholds,
                        AuditService auditService) {
        this.vitalRepository = vitalRepository;
        this.patientRepository = patientRepository;
        this.thresholds = thresholds;
        this.auditService = auditService;
    }

    @Transactional
    public VitalResponse record(Long patientId, VitalRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        VitalEntity vital = new VitalEntity();
        vital.setPatient(patient);
        vital.setSystolic(request.systolic());
        vital.setDiastolic(request.diastolic());
        vital.setHr(request.hr());
        vital.setTemp(request.temp());
        vital.setSpo2(request.spo2());
        vital.setNotes(request.notes());
        vital.setCreatedBy(currentUser);

        boolean alert = isOutOfRange(request);
        vital.setHasAlert(alert);
        vital.setAlertResolved(!alert);

        vitalRepository.save(vital);

        VitalResponse response = toResponse(vital);
        auditService.record(patientId, "VITALS", response);

        if (alert) log.warn("Vital alert triggered for patient {}", patientId);
        else log.info("Vitals recorded for patient {} by {}", patientId, currentUser);

        return toResponse(vital);
    }

    public List<VitalResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return vitalRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    private boolean isOutOfRange(VitalRequest r) {
        if (r.systolic()  != null && (r.systolic()  < thresholds.getSystolicMin()  || r.systolic()  > thresholds.getSystolicMax()))  return true;
        if (r.diastolic() != null && (r.diastolic() < thresholds.getDiastolicMin() || r.diastolic() > thresholds.getDiastolicMax())) return true;
        if (r.hr()        != null && (r.hr()        < thresholds.getHrMin()        || r.hr()        > thresholds.getHrMax()))        return true;
        if (r.spo2()      != null && r.spo2() < thresholds.getSpo2Min())                                                             return true;
        if (r.temp()      != null && (r.temp().doubleValue() < thresholds.getTempMin() || r.temp().doubleValue() > thresholds.getTempMax())) return true;
        return false;
    }

    private VitalResponse toResponse(VitalEntity v) {
        return new VitalResponse(v.getId(), v.getSystolic(), v.getDiastolic(),
                v.getHr(), v.getTemp(), v.getSpo2(), v.getNotes(),
                v.isHasAlert(), v.isAlertResolved(),
                v.getPatient().getId(), v.getCreatedOn());
    }
}
