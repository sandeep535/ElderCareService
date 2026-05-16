-- ── Create Table ─────────────────────────────────────────────────────────────
CREATE TABLE vital_metric_master (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id     INT          NOT NULL UNIQUE,
    field_key     VARCHAR(100) NOT NULL UNIQUE,
    display_name  VARCHAR(150) NOT NULL,
    unit          VARCHAR(50),
    category      VARCHAR(100),
    mandatory     TINYINT(1)   NOT NULL DEFAULT 0,
    display       TINYINT(1)   NOT NULL DEFAULT 1,
    low_value     DECIMAL(10,2),
    high_value    DECIMAL(10,2),
    normal_range  VARCHAR(100),
    sort_order    INT,
    active        TINYINT(1)   NOT NULL DEFAULT 1,
    created_by    VARCHAR(100),
    created_on    DATETIME,
    updated_by    VARCHAR(100),
    updated_on    DATETIME
);

-- ── Seed all 22 metrics ───────────────────────────────────────────────────────
-- Columns: device_id, field_key, display_name, unit, category,
--          mandatory, display, low_value, high_value, normal_range, sort_order, active

INSERT INTO vital_metric_master
    (device_id, field_key, display_name, unit, category, mandatory, display, low_value, high_value, normal_range, sort_order, active)
VALUES
-- Body Composition
(1,  'height',                      'Height',                       'cm',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           1,  1),
(2,  'weight',                      'Weight',                       'kg',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           2,  1),
(3,  'bmi',                         'BMI',                          'N.A.', 'BODY_COMPOSITION', 0, 1, 18.5,  24.9,  '18.5 - 24.9',  3,  1),
(4,  'body_fat_percentage',         'Body Fat Percentage',          '%',    'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           4,  1),
(5,  'body_fat_mass',               'Body Fat Mass',                'kg',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           5,  1),
(6,  'skeletal_muscle_percentage',  'Skeletal Muscle Percentage',   '%',    'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           6,  1),
(7,  'body_water_percentage',       'Body Water Percentage',        '%',    'BODY_COMPOSITION', 0, 1, 45.0,  65.0,  '45 - 65',      7,  1),
(8,  'total_moisture',              'Total Moisture',               'kg',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           8,  1),
(9,  'extracellular_water_pct',     'Extracellular Water %',        '%',    'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           9,  1),
(10, 'intracellular_water_pct',     'Intracellular Water %',        '%',    'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           10, 1),
(11, 'basal_metabolism',            'Basal Metabolism',             'kcal', 'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           11, 1),
(12, 'visceral_fat_level',          'Visceral Fat Level',           'N.A.', 'BODY_COMPOSITION', 0, 1, NULL,  12.0,  '< 12',         12, 1),
(13, 'protein',                     'Protein',                      'kg',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           13, 1),
(14, 'mineral',                     'Mineral',                      'kg',   'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           14, 1),
(15, 'body_age',                    'Body Age',                     'N.A.', 'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           15, 1),
(16, 'overall',                     'Overall Score',                'N.A.', 'BODY_COMPOSITION', 0, 1, NULL,  NULL,  NULL,           16, 1),

-- Clinical Vitals
(17, 'temperature',                 'Temperature',                  '°C',   'CLINICAL',         1, 1, 36.1,  37.2,  '36.1 - 37.2',  17, 1),
(18, 'systolic',                    'Systolic BP',                  'mmHg', 'CLINICAL',         1, 1, 90.0,  139.0, '90 - 139',     18, 1),
(19, 'diastolic',                   'Diastolic BP',                 'mmHg', 'CLINICAL',         1, 1, 60.0,  89.0,  '60 - 89',      19, 1),
(20, 'bp_heart_rate',               'BP Heart Rate',                'bpm',  'CLINICAL',         1, 1, 60.0,  100.0, '60 - 100',     20, 1),
(21, 'spo2',                        'SpO2',                         '%',    'CLINICAL',         1, 1, 95.0,  NULL,  '≥ 95',         21, 1),
(22, 'spo2_heart_rate',             'SpO2 Heart Rate',              'bpm',  'CLINICAL',         1, 1, 60.0,  100.0, '60 - 100',     22, 1);
