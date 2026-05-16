package com.eldercare.service.service;

import com.eldercare.service.dto.VitalRequest;
import com.eldercare.service.dto.VitalResponse;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.entity.VitalEntity;
import com.eldercare.service.entity.VitalMetricMasterEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientRepository;
import com.eldercare.service.repository.VitalRepository;
import com.eldercare.service.repository.VitalMetricMasterRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VitalService {

    private static final Logger log = LogManager.getLogger(VitalService.class);

    private final VitalRepository vitalRepository;
    private final PatientRepository patientRepository;
    private final VitalMetricMasterRepository vitalMetricMasterRepository;
    private final AlertService alertService;
    private final AuditService auditService;

    public VitalService(VitalRepository vitalRepository,
                        PatientRepository patientRepository,
                        VitalMetricMasterRepository vitalMetricMasterRepository,
                        AlertService alertService,
                        AuditService auditService) {
        this.vitalRepository = vitalRepository;
        this.patientRepository = patientRepository;
        this.vitalMetricMasterRepository = vitalMetricMasterRepository;
        this.alertService = alertService;
        this.auditService = auditService;
    }

    @Transactional
    public VitalResponse record(Long patientId, VitalRequest request) {
        PatientEntity patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", patientId));

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        VitalEntity vital = new VitalEntity();
        vital.setPatient(patient);
        mapRequestToEntity(request, vital);
        vital.setCreatedBy(currentUser);

        boolean alert = checkForAlerts(request);
        vital.setHasAlert(alert);
        vital.setAlertResolved(!alert);

        vitalRepository.save(vital);

        VitalResponse response = toResponse(vital);
        auditService.record(patientId, "VITALS", response);

        if (alert) {
            saveVitalAlerts(patientId, request, currentUser);
            log.warn("Vital alert triggered for patient {}", patientId);
        } else {
            log.info("Vitals recorded for patient {} by {}", patientId, currentUser);
        }

        return response;
    }

    private void mapRequestToEntity(VitalRequest request, VitalEntity vital) {
        // Body Composition Fields
        vital.setHeight(request.height());
        vital.setWeight(request.weight());
        vital.setBmi(request.bmi());
        vital.setBodyFatPercentage(request.bodyFatPercentage());
        vital.setBodyFatMass(request.bodyFatMass());
        vital.setSkeletalMusclePercentage(request.skeletalMusclePercentage());
        vital.setBodyWaterPercentage(request.bodyWaterPercentage());
        vital.setTotalMoisture(request.totalMoisture());
        vital.setExtracellularWaterPct(request.extracellularWaterPct());
        vital.setIntracellularWaterPct(request.intracellularWaterPct());
        vital.setBasalMetabolism(request.basalMetabolism());
        vital.setVisceralFatLevel(request.visceralFatLevel());
        vital.setProtein(request.protein());
        vital.setMineral(request.mineral());
        vital.setBodyAge(request.bodyAge());
        vital.setOverall(request.overall());
        
        // Clinical Vitals Fields
        vital.setTemperature(request.temperature());
        vital.setSystolic(request.systolic());
        vital.setDiastolic(request.diastolic());
        vital.setBpHeartRate(request.bpHeartRate());
        vital.setSpo2(request.spo2());
        vital.setSpo2HeartRate(request.spo2HeartRate());
        
        vital.setNotes(request.notes());
    }

    private boolean checkForAlerts(VitalRequest request) {
        List<VitalMetricMasterEntity> activeMetrics = vitalMetricMasterRepository.findByActiveTrueOrderBySortOrderAsc();
        Map<String, VitalMetricMasterEntity> metricMap = activeMetrics.stream()
                .collect(Collectors.toMap(VitalMetricMasterEntity::getFieldKey, Function.identity()));

        return checkVitalValue("height", request.height(), metricMap) ||
               checkVitalValue("weight", request.weight(), metricMap) ||
               checkVitalValue("bmi", request.bmi(), metricMap) ||
               checkVitalValue("body_fat_percentage", request.bodyFatPercentage(), metricMap) ||
               checkVitalValue("body_fat_mass", request.bodyFatMass(), metricMap) ||
               checkVitalValue("skeletal_muscle_percentage", request.skeletalMusclePercentage(), metricMap) ||
               checkVitalValue("body_water_percentage", request.bodyWaterPercentage(), metricMap) ||
               checkVitalValue("total_moisture", request.totalMoisture(), metricMap) ||
               checkVitalValue("extracellular_water_pct", request.extracellularWaterPct(), metricMap) ||
               checkVitalValue("intracellular_water_pct", request.intracellularWaterPct(), metricMap) ||
               checkVitalValue("basal_metabolism", request.basalMetabolism(), metricMap) ||
               checkVitalValue("visceral_fat_level", request.visceralFatLevel(), metricMap) ||
               checkVitalValue("protein", request.protein(), metricMap) ||
               checkVitalValue("mineral", request.mineral(), metricMap) ||
               checkVitalValue("body_age", request.bodyAge(), metricMap) ||
               checkVitalValue("overall", request.overall(), metricMap) ||
               checkVitalValue("temperature", request.temperature(), metricMap) ||
               checkVitalValue("systolic", request.systolic(), metricMap) ||
               checkVitalValue("diastolic", request.diastolic(), metricMap) ||
               checkVitalValue("bp_heart_rate", request.bpHeartRate(), metricMap) ||
               checkVitalValue("spo2", request.spo2(), metricMap) ||
               checkVitalValue("spo2_heart_rate", request.spo2HeartRate(), metricMap);
    }

    private boolean checkVitalValue(String fieldKey, Object value, Map<String, VitalMetricMasterEntity> metricMap) {
        if (value == null) return false;
        
        VitalMetricMasterEntity metric = metricMap.get(fieldKey);
        if (metric == null || metric.getLowValue() == null && metric.getHighValue() == null) {
            return false;
        }

        BigDecimal numericValue;
        if (value instanceof Integer) {
            numericValue = BigDecimal.valueOf((Integer) value);
        } else if (value instanceof BigDecimal) {
            numericValue = (BigDecimal) value;
        } else {
            return false;
        }

        if (metric.getLowValue() != null && numericValue.compareTo(metric.getLowValue()) < 0) {
            return true;
        }
        if (metric.getHighValue() != null && numericValue.compareTo(metric.getHighValue()) > 0) {
            return true;
        }
        
        return false;
    }

    private void saveVitalAlerts(Long patientId, VitalRequest request, String createdBy) {
        List<VitalMetricMasterEntity> activeMetrics = vitalMetricMasterRepository.findByActiveTrueOrderBySortOrderAsc();
        Map<String, VitalMetricMasterEntity> metricMap = activeMetrics.stream()
                .collect(Collectors.toMap(VitalMetricMasterEntity::getFieldKey, Function.identity()));

        saveAlertIfOutOfRange(patientId, "height", request.height(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "weight", request.weight(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "bmi", request.bmi(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "body_fat_percentage", request.bodyFatPercentage(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "body_fat_mass", request.bodyFatMass(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "skeletal_muscle_percentage", request.skeletalMusclePercentage(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "body_water_percentage", request.bodyWaterPercentage(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "total_moisture", request.totalMoisture(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "extracellular_water_pct", request.extracellularWaterPct(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "intracellular_water_pct", request.intracellularWaterPct(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "basal_metabolism", request.basalMetabolism(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "visceral_fat_level", request.visceralFatLevel(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "protein", request.protein(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "mineral", request.mineral(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "body_age", request.bodyAge(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "overall", request.overall(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "temperature", request.temperature(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "systolic", request.systolic(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "diastolic", request.diastolic(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "bp_heart_rate", request.bpHeartRate(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "spo2", request.spo2(), metricMap, createdBy);
        saveAlertIfOutOfRange(patientId, "spo2_heart_rate", request.spo2HeartRate(), metricMap, createdBy);
    }

    private void saveAlertIfOutOfRange(Long patientId, String fieldKey, Object value, 
                                     Map<String, VitalMetricMasterEntity> metricMap, String createdBy) {
        if (value == null) return;
        
        VitalMetricMasterEntity metric = metricMap.get(fieldKey);
        if (metric == null) return;

        BigDecimal numericValue;
        if (value instanceof Integer) {
            numericValue = BigDecimal.valueOf((Integer) value);
        } else if (value instanceof BigDecimal) {
            numericValue = (BigDecimal) value;
        } else {
            return;
        }

        String severity = "CLINICAL".equals(metric.getCategory()) ? "HIGH" : "MEDIUM";
        
        if (metric.getLowValue() != null && numericValue.compareTo(metric.getLowValue()) < 0) {
            alertService.saveAlert(patientId, "VITALS", metric.getDisplayName(),
                    String.valueOf(value), 
                    "Below normal (< " + metric.getLowValue() + " " + metric.getUnit() + ")", 
                    severity, createdBy);
        }
        
        if (metric.getHighValue() != null && numericValue.compareTo(metric.getHighValue()) > 0) {
            alertService.saveAlert(patientId, "VITALS", metric.getDisplayName(),
                    String.valueOf(value), 
                    "Above normal (> " + metric.getHighValue() + " " + metric.getUnit() + ")", 
                    severity, createdBy);
        }
    }

    public List<VitalResponse> getByPatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return vitalRepository.findByPatientIdOrderByCreatedOnDesc(patientId)
                .stream().map(this::toResponse).toList();
    }

    public VitalResponse getLatest(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient", patientId);
        }
        return vitalRepository.findFirstByPatientIdOrderByCreatedOnDesc(patientId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No vitals found for patient", patientId));
    }

    private VitalResponse toResponse(VitalEntity v) {
        return new VitalResponse(
                v.getId(),
                // Body Composition Fields
                v.getHeight(), v.getWeight(), v.getBmi(), v.getBodyFatPercentage(),
                v.getBodyFatMass(), v.getSkeletalMusclePercentage(), v.getBodyWaterPercentage(),
                v.getTotalMoisture(), v.getExtracellularWaterPct(), v.getIntracellularWaterPct(),
                v.getBasalMetabolism(), v.getVisceralFatLevel(), v.getProtein(), v.getMineral(),
                v.getBodyAge(), v.getOverall(),
                // Clinical Vitals Fields
                v.getTemperature(), v.getSystolic(), v.getDiastolic(), v.getBpHeartRate(),
                v.getSpo2(), v.getSpo2HeartRate(),
                // Common Fields
                v.getNotes(), v.isHasAlert(), v.isAlertResolved(),
                v.getPatient().getId(), v.getCreatedOn()
        );
    }
}
