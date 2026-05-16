-- =====================================================
-- SAMPLE VITAL DATA INSERTION SCRIPT
-- Tests all 22 vital metric fields with realistic data
-- =====================================================

-- Sample 1: Complete Body Composition + Clinical Vitals
INSERT INTO vital (
    patient_id,
    -- Body Composition Fields
    height, weight, bmi, body_fat_percentage, body_fat_mass,
    skeletal_muscle_percentage, body_water_percentage, total_moisture,
    extracellular_water_pct, intracellular_water_pct, basal_metabolism,
    visceral_fat_level, protein, mineral, body_age, overall,
    -- Clinical Vitals Fields  
    temperature, systolic, diastolic, bp_heart_rate, spo2, spo2_heart_rate,
    -- Common Fields
    notes, has_alert, alert_resolved, created_by, created_on
) VALUES (
    1, -- Assuming patient ID 1 exists
    -- Body Composition
    170.5, 70.2, 24.3, 15.8, 11.1,
    42.5, 58.2, 40.8,
    38.5, 61.5, 1650,
    8.5, 12.5, 3.2, 25, 85,
    -- Clinical Vitals
    36.8, 120, 80, 72, 98, 75,
    -- Common
    'Complete health assessment - all vitals normal', false, true, 'nurse1', NOW()
);

-- Sample 2: Only Clinical Vitals (typical vital signs check)
INSERT INTO vital (
    patient_id,
    temperature, systolic, diastolic, bp_heart_rate, spo2, spo2_heart_rate,
    notes, has_alert, alert_resolved, created_by, created_on
) VALUES (
    1,
    37.2, 130, 85, 78, 97, 80,
    'Routine vital signs check', false, true, 'nurse2', NOW()
);

-- Sample 3: High BP Alert Case
INSERT INTO vital (
    patient_id,
    temperature, systolic, diastolic, bp_heart_rate, spo2,
    notes, has_alert, alert_resolved, created_by, created_on
) VALUES (
    1,
    36.9, 160, 95, 88, 96,
    'Patient showing elevated blood pressure', true, false, 'doctor1', NOW()
);

-- Sample 4: Body Composition Only (from body analyzer machine)
INSERT INTO vital (
    patient_id,
    height, weight, bmi, body_fat_percentage, body_water_percentage,
    visceral_fat_level, basal_metabolism, body_age, overall,
    notes, has_alert, alert_resolved, created_by, created_on
) VALUES (
    1,
    168.0, 75.5, 26.8, 22.3, 55.1,
    12.2, 1580, 35, 72,
    'Body composition analysis from InBody scanner', false, true, 'nurse3', NOW()
);

-- Sample 5: Low SpO2 Alert Case
INSERT INTO vital (
    patient_id,
    temperature, systolic, diastolic, spo2, spo2_heart_rate,
    notes, has_alert, alert_resolved, created_by, created_on
) VALUES (
    1,
    36.5, 115, 75, 92, 85,
    'Patient experiencing breathing difficulty', true, false, 'nurse1', NOW()
);

-- Verify the inserted data
SELECT 
    id,
    patient_id,
    -- Body Composition (showing key fields)
    height, weight, bmi, body_fat_percentage,
    -- Clinical Vitals
    temperature, systolic, diastolic, spo2,
    -- Status
    notes, has_alert, created_by, created_on
FROM vital 
WHERE patient_id = 1 
ORDER BY created_on DESC;

-- Check for alerts
SELECT 
    id,
    patient_id,
    systolic, diastolic, spo2, temperature,
    notes,
    has_alert,
    alert_resolved,
    created_on
FROM vital 
WHERE has_alert = true
ORDER BY created_on DESC;