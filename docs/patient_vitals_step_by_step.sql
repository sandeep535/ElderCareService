-- =====================================================
-- VITAL TABLE MIGRATION - STEP BY STEP APPROACH
-- Run each section separately to avoid safe update mode issues
-- =====================================================

-- STEP 1: Add Body Composition Fields
ALTER TABLE vital ADD COLUMN height DECIMAL(5,2) NULL COMMENT 'Height in cm';
ALTER TABLE vital ADD COLUMN weight DECIMAL(5,2) NULL COMMENT 'Weight in kg';
ALTER TABLE vital ADD COLUMN bmi DECIMAL(4,2) NULL COMMENT 'Body Mass Index';
ALTER TABLE vital ADD COLUMN body_fat_percentage DECIMAL(5,2) NULL COMMENT 'Body Fat Percentage';
ALTER TABLE vital ADD COLUMN body_fat_mass DECIMAL(5,2) NULL COMMENT 'Body Fat Mass in kg';

-- STEP 2: Add More Body Composition Fields
ALTER TABLE vital ADD COLUMN skeletal_muscle_percentage DECIMAL(5,2) NULL COMMENT 'Skeletal Muscle Percentage';
ALTER TABLE vital ADD COLUMN body_water_percentage DECIMAL(5,2) NULL COMMENT 'Body Water Percentage';
ALTER TABLE vital ADD COLUMN total_moisture DECIMAL(5,2) NULL COMMENT 'Total Moisture in kg';
ALTER TABLE vital ADD COLUMN extracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Extracellular Water Percentage';
ALTER TABLE vital ADD COLUMN intracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Intracellular Water Percentage';

-- STEP 3: Add Remaining Body Composition Fields
ALTER TABLE vital ADD COLUMN basal_metabolism INT NULL COMMENT 'Basal Metabolism in kcal';
ALTER TABLE vital ADD COLUMN visceral_fat_level DECIMAL(4,1) NULL COMMENT 'Visceral Fat Level';
ALTER TABLE vital ADD COLUMN protein DECIMAL(5,2) NULL COMMENT 'Protein in kg';
ALTER TABLE vital ADD COLUMN mineral DECIMAL(5,2) NULL COMMENT 'Mineral in kg';
ALTER TABLE vital ADD COLUMN body_age INT NULL COMMENT 'Body Age';
ALTER TABLE vital ADD COLUMN overall INT NULL COMMENT 'Overall Score';

-- STEP 4: Add Clinical Vitals Fields
ALTER TABLE vital ADD COLUMN temperature DECIMAL(4,1) NULL COMMENT 'Temperature in °C';
ALTER TABLE vital ADD COLUMN bp_heart_rate INT NULL COMMENT 'BP Heart Rate in bpm';
ALTER TABLE vital ADD COLUMN spo2_heart_rate INT NULL COMMENT 'SpO2 Heart Rate in bpm';

-- STEP 5: Copy existing temp data (if you have existing data)
-- Option A: If you have existing records, run this to copy temp to temperature
-- UPDATE vital SET temperature = temp WHERE id IN (SELECT id FROM (SELECT id FROM vital WHERE temp IS NOT NULL) AS temp_table);

-- Option B: If safe mode is still an issue, disable it first:
-- SET SQL_SAFE_UPDATES = 0;
-- UPDATE vital SET temperature = temp WHERE temp IS NOT NULL;
-- SET SQL_SAFE_UPDATES = 1;

-- Option C: Update records one by one (if you have few records)
-- UPDATE vital SET temperature = temp WHERE id = 1 AND temp IS NOT NULL;
-- UPDATE vital SET temperature = temp WHERE id = 2 AND temp IS NOT NULL;
-- ... continue for each record

-- STEP 6: Add Performance Indexes
CREATE INDEX idx_vital_patient_created ON vital(patient_id, created_on DESC);
CREATE INDEX idx_vital_has_alert ON vital(has_alert, created_on DESC);
CREATE INDEX idx_vital_temperature ON vital(temperature);
CREATE INDEX idx_vital_systolic ON vital(systolic);
CREATE INDEX idx_vital_diastolic ON vital(diastolic);
CREATE INDEX idx_vital_spo2 ON vital(spo2);

-- STEP 7: Verify Structure
DESCRIBE vital;

-- STEP 8: Check if all columns were added
SELECT COUNT(*) as total_columns 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'vital' 
AND TABLE_SCHEMA = DATABASE();

-- Should show all the new columns
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'vital' 
AND TABLE_SCHEMA = DATABASE()
AND COLUMN_NAME IN (
    'height', 'weight', 'bmi', 'body_fat_percentage', 'body_fat_mass',
    'skeletal_muscle_percentage', 'body_water_percentage', 'total_moisture',
    'extracellular_water_pct', 'intracellular_water_pct', 'basal_metabolism',
    'visceral_fat_level', 'protein', 'mineral', 'body_age', 'overall',
    'temperature', 'bp_heart_rate', 'spo2_heart_rate'
);