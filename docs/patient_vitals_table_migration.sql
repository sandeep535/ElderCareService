-- =====================================================
-- VITAL TABLE MIGRATION SCRIPT
-- Adds all 22 vital metric fields to existing vital table
-- =====================================================

-- Add Body Composition Fields (16 fields)
ALTER TABLE vital ADD COLUMN height DECIMAL(5,2) NULL COMMENT 'Height in cm';
ALTER TABLE vital ADD COLUMN weight DECIMAL(5,2) NULL COMMENT 'Weight in kg';
ALTER TABLE vital ADD COLUMN bmi DECIMAL(4,2) NULL COMMENT 'Body Mass Index';
ALTER TABLE vital ADD COLUMN body_fat_percentage DECIMAL(5,2) NULL COMMENT 'Body Fat Percentage';
ALTER TABLE vital ADD COLUMN body_fat_mass DECIMAL(5,2) NULL COMMENT 'Body Fat Mass in kg';
ALTER TABLE vital ADD COLUMN skeletal_muscle_percentage DECIMAL(5,2) NULL COMMENT 'Skeletal Muscle Percentage';
ALTER TABLE vital ADD COLUMN body_water_percentage DECIMAL(5,2) NULL COMMENT 'Body Water Percentage';
ALTER TABLE vital ADD COLUMN total_moisture DECIMAL(5,2) NULL COMMENT 'Total Moisture in kg';
ALTER TABLE vital ADD COLUMN extracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Extracellular Water Percentage';
ALTER TABLE vital ADD COLUMN intracellular_water_pct DECIMAL(5,2) NULL COMMENT 'Intracellular Water Percentage';
ALTER TABLE vital ADD COLUMN basal_metabolism INT NULL COMMENT 'Basal Metabolism in kcal';
ALTER TABLE vital ADD COLUMN visceral_fat_level DECIMAL(4,1) NULL COMMENT 'Visceral Fat Level';
ALTER TABLE vital ADD COLUMN protein DECIMAL(5,2) NULL COMMENT 'Protein in kg';
ALTER TABLE vital ADD COLUMN mineral DECIMAL(5,2) NULL COMMENT 'Mineral in kg';
ALTER TABLE vital ADD COLUMN body_age INT NULL COMMENT 'Body Age';
ALTER TABLE vital ADD COLUMN overall INT NULL COMMENT 'Overall Score';

-- Add Clinical Vitals Fields (6 fields)
ALTER TABLE vital ADD COLUMN temperature DECIMAL(4,1) NULL COMMENT 'Temperature in °C';
ALTER TABLE vital ADD COLUMN bp_heart_rate INT NULL COMMENT 'BP Heart Rate in bpm';
ALTER TABLE vital ADD COLUMN spo2_heart_rate INT NULL COMMENT 'SpO2 Heart Rate in bpm';

-- Update existing data (copy old temp column to new temperature column if exists)
-- Only run this if you have existing data in 'temp' column
UPDATE vital SET temperature = temp WHERE temp IS NOT NULL;

-- Optional: Drop old columns if you want to clean up
-- Uncomment these lines if you want to remove the old columns
-- ALTER TABLE vital DROP COLUMN temp;
-- ALTER TABLE vital DROP COLUMN hr;

-- Add indexes for better performance
CREATE INDEX idx_vital_patient_created ON vital(patient_id, created_on DESC);
CREATE INDEX idx_vital_has_alert ON vital(has_alert, created_on DESC);
CREATE INDEX idx_vital_temperature ON vital(temperature);
CREATE INDEX idx_vital_systolic ON vital(systolic);
CREATE INDEX idx_vital_diastolic ON vital(diastolic);
CREATE INDEX idx_vital_spo2 ON vital(spo2);

-- Verify the table structure
DESCRIBE vital;