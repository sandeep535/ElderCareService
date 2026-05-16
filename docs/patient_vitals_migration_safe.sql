-- =====================================================
-- VITAL TABLE MIGRATION SCRIPT (SAFE MODE COMPATIBLE)
-- Adds all 22 vital metric fields to existing vital table
-- =====================================================

-- Disable safe update mode temporarily (if you have permission)
SET SQL_SAFE_UPDATES = 0;

-- Add Body Composition Fields (16 fields)
ALTER TABLE vital ADD COLUMN IF NOT EXISTS height DECIMAL(5,2) NULL COMMENT 'Height in cm';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS weight DECIMAL(5,2) NULL COMMENT 'Weight in kg';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS bmi DECIMAL(4,2) NULL COMMENT 'Body Mass Index';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS body_fat_percentage DECIMAL(5,2) NULL COMMENT 'Body Fat Percentage';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS body_fat_mass DECIMAL(5,2) NULL COMMENT 'Body Fat Mass in kg';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS skeletal_muscle_percentage DECIMAL(5,2) NULL COMMENT 'Skeletal Muscle Percentage';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS body_water_percentage DECIMAL(5,2) NULL COMMENT 'Body Water Percentage';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS total_moisture DECIMAL(5,2) NULL COMMENT 'Total Moisture in kg';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS extracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Extracellular Water Percentage';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS intracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Intracellular Water Percentage';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS basal_metabolism INT NULL COMMENT 'Basal Metabolism in kcal';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS visceral_fat_level DECIMAL(4,1) NULL COMMENT 'Visceral Fat Level';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS protein DECIMAL(5,2) NULL COMMENT 'Protein in kg';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS mineral DECIMAL(5,2) NULL COMMENT 'Mineral in kg';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS body_age INT NULL COMMENT 'Body Age';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS overall INT NULL COMMENT 'Overall Score';

-- Add Clinical Vitals Fields (6 fields)
ALTER TABLE vital ADD COLUMN IF NOT EXISTS temperature DECIMAL(4,1) NULL COMMENT 'Temperature in °C';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS bp_heart_rate INT NULL COMMENT 'BP Heart Rate in bpm';
ALTER TABLE vital ADD COLUMN IF NOT EXISTS spo2_heart_rate INT NULL COMMENT 'SpO2 Heart Rate in bpm';

-- Update existing data (copy old temp column to new temperature column if exists)
-- Using primary key in WHERE clause to satisfy safe update mode
UPDATE vital SET temperature = temp WHERE id > 0 AND temp IS NOT NULL;

-- Re-enable safe update mode
SET SQL_SAFE_UPDATES = 1;

-- Add indexes for better performance
CREATE INDEX IF NOT EXISTS idx_vital_patient_created ON vital(patient_id, created_on DESC);
CREATE INDEX IF NOT EXISTS idx_vital_has_alert ON vital(has_alert, created_on DESC);
CREATE INDEX IF NOT EXISTS idx_vital_temperature ON vital(temperature);
CREATE INDEX IF NOT EXISTS idx_vital_systolic ON vital(systolic);
CREATE INDEX IF NOT EXISTS idx_vital_diastolic ON vital(diastolic);
CREATE INDEX IF NOT EXISTS idx_vital_spo2 ON vital(spo2);

-- Verify the table structure
DESCRIBE vital;