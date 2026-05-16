package com.eldercare.service.service;

import com.eldercare.service.dto.ThirdPartyVitalRequest;
import com.eldercare.service.dto.VitalRequest;
import com.eldercare.service.dto.VitalResponse;
import com.eldercare.service.entity.PatientEntity;
import com.eldercare.service.exception.ResourceNotFoundException;
import com.eldercare.service.repository.PatientRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class ThirdPartyVitalService {

    private static final Logger log = LogManager.getLogger(ThirdPartyVitalService.class);

    private final VitalService vitalService;
    private final PatientRepository patientRepository;

    // Device ID to Field Mapping
    private static final Map<Integer, String> DEVICE_FIELD_MAPPING = new HashMap<>();
    
    static {
        // Body Composition Fields (1-16)
        DEVICE_FIELD_MAPPING.put(1, "height");
        DEVICE_FIELD_MAPPING.put(2, "weight");
        DEVICE_FIELD_MAPPING.put(3, "bmi");
        DEVICE_FIELD_MAPPING.put(4, "bodyFatPercentage");
        DEVICE_FIELD_MAPPING.put(5, "bodyFatMass");
        DEVICE_FIELD_MAPPING.put(6, "skeletalMusclePercentage");
        DEVICE_FIELD_MAPPING.put(7, "bodyWaterPercentage");
        DEVICE_FIELD_MAPPING.put(8, "totalMoisture");
        DEVICE_FIELD_MAPPING.put(9, "extracellularWaterPct");
        DEVICE_FIELD_MAPPING.put(10, "intracellularWaterPct");
        DEVICE_FIELD_MAPPING.put(11, "basalMetabolism");
        DEVICE_FIELD_MAPPING.put(12, "visceralFatLevel");
        DEVICE_FIELD_MAPPING.put(13, "protein");
        DEVICE_FIELD_MAPPING.put(14, "mineral");
        DEVICE_FIELD_MAPPING.put(15, "bodyAge");
        DEVICE_FIELD_MAPPING.put(16, "overall");
        
        // Clinical Vitals Fields (17-22)
        DEVICE_FIELD_MAPPING.put(17, "temperature");
        DEVICE_FIELD_MAPPING.put(18, "systolic");
        DEVICE_FIELD_MAPPING.put(19, "diastolic");
        DEVICE_FIELD_MAPPING.put(20, "bpHeartRate");
        DEVICE_FIELD_MAPPING.put(21, "spo2");
        DEVICE_FIELD_MAPPING.put(22, "spo2HeartRate");
    }

    public ThirdPartyVitalService(VitalService vitalService, PatientRepository patientRepository) {
        this.vitalService = vitalService;
        this.patientRepository = patientRepository;
    }

    @Transactional
    public VitalResponse processThirdPartyVitals(ThirdPartyVitalRequest request) {
        log.info("Processing third-party vitals for patient: {}", request.patientId());

        // Find patient by external patient ID
        PatientEntity patient = findPatientByExternalId(request.patientId());
        
        // Convert third-party format to internal VitalRequest
        VitalRequest vitalRequest = convertToVitalRequest(request);
        
        // Save vitals using existing service
        VitalResponse response = vitalService.record(patient.getId(), vitalRequest);
        
        log.info("Successfully processed {} vital readings for patient {}", 
                request.readings().size(), request.patientId());
        
        return response;
    }

    private PatientEntity findPatientByExternalId(String externalPatientId) {
        // Find by patient ID
        return patientRepository.findByPatientId(externalPatientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with external ID: " + externalPatientId));
    }

    private VitalRequest convertToVitalRequest(ThirdPartyVitalRequest request) {
        Map<String, Object> vitalData = new HashMap<>();
        
        // Process each reading and map to internal field names
        for (ThirdPartyVitalRequest.VitalReading reading : request.readings()) {
            String fieldName = DEVICE_FIELD_MAPPING.get(reading.deviceId());
            
            if (fieldName != null) {
                Object convertedValue = convertValue(fieldName, reading.value());
                vitalData.put(fieldName, convertedValue);
                
                log.debug("Mapped device_id {} ({}) -> {} = {}", 
                        reading.deviceId(), fieldName, reading.value(), convertedValue);
            } else {
                log.warn("Unknown device_id: {} with value: {}", reading.deviceId(), reading.value());
            }
        }
        
        // Build VitalRequest with mapped values
        return new VitalRequest(
                // Body Composition Fields
                (BigDecimal) vitalData.get("height"),
                (BigDecimal) vitalData.get("weight"),
                (BigDecimal) vitalData.get("bmi"),
                (BigDecimal) vitalData.get("bodyFatPercentage"),
                (BigDecimal) vitalData.get("bodyFatMass"),
                (BigDecimal) vitalData.get("skeletalMusclePercentage"),
                (BigDecimal) vitalData.get("bodyWaterPercentage"),
                (BigDecimal) vitalData.get("totalMoisture"),
                (BigDecimal) vitalData.get("extracellularWaterPct"),
                (BigDecimal) vitalData.get("intracellularWaterPct"),
                (Integer) vitalData.get("basalMetabolism"),
                (BigDecimal) vitalData.get("visceralFatLevel"),
                (BigDecimal) vitalData.get("protein"),
                (BigDecimal) vitalData.get("mineral"),
                (Integer) vitalData.get("bodyAge"),
                (Integer) vitalData.get("overall"),
                
                // Clinical Vitals Fields
                (BigDecimal) vitalData.get("temperature"),
                (Integer) vitalData.get("systolic"),
                (Integer) vitalData.get("diastolic"),
                (Integer) vitalData.get("bpHeartRate"),
                (Integer) vitalData.get("spo2"),
                (Integer) vitalData.get("spo2HeartRate"),
                
                // Notes
                "Third-party vitals recorded at " + request.timestamp()
        );
    }

    private Object convertValue(String fieldName, Object value) {
        if (value == null) return null;
        
        // Integer fields
        if (isIntegerField(fieldName)) {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.valueOf(value.toString());
        }
        
        // BigDecimal fields
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        
        return new BigDecimal(value.toString());
    }

    private boolean isIntegerField(String fieldName) {
        return fieldName.equals("basalMetabolism") || 
               fieldName.equals("bodyAge") || 
               fieldName.equals("overall") ||
               fieldName.equals("systolic") || 
               fieldName.equals("diastolic") || 
               fieldName.equals("bpHeartRate") || 
               fieldName.equals("spo2") || 
               fieldName.equals("spo2HeartRate");
    }
}