-- ============================================================
-- MASTER TABLE SEED DATA
-- ============================================================

DELETE FROM master_table WHERE type = 'BLOOD_TYPE';
INSERT INTO master_table (lookup_value, lookup_item, lookup_code, type, active) VALUES
('1', 'A+',  'A_POS',  'BLOOD_TYPE', true),
('2', 'A-',  'A_NEG',  'BLOOD_TYPE', true),
('3', 'B+',  'B_POS',  'BLOOD_TYPE', true),
('4', 'B-',  'B_NEG',  'BLOOD_TYPE', true),
('5', 'AB+', 'AB_POS', 'BLOOD_TYPE', true),
('6', 'AB-', 'AB_NEG', 'BLOOD_TYPE', true),
('7', 'O+',  'O_POS',  'BLOOD_TYPE', true),
('8', 'O-',  'O_NEG',  'BLOOD_TYPE', true);

DELETE FROM master_table WHERE type = 'RELATIONSHIP';
INSERT INTO master_table (lookup_value, lookup_item, lookup_code, type, active) VALUES
('1', 'Spouse',     'SPOUSE',     'RELATIONSHIP', true),
('2', 'Child',      'CHILD',      'RELATIONSHIP', true),
('3', 'Parent',     'PARENT',     'RELATIONSHIP', true),
('4', 'Sibling',    'SIBLING',    'RELATIONSHIP', true),
('5', 'Grandchild', 'GRANDCHILD', 'RELATIONSHIP', true),
('6', 'Friend',     'FRIEND',     'RELATIONSHIP', true);

DELETE FROM master_table WHERE type = 'PATIENT_STATUS';
INSERT INTO master_table (lookup_value, lookup_item, lookup_code, type, active) VALUES
('1', 'Stable',      'STABLE',      'PATIENT_STATUS', true),
('2', 'Monitor',     'WARNING',     'PATIENT_STATUS', true),
('3', 'Critical',    'CRITICAL',    'PATIENT_STATUS', true),
('4', 'Observation', 'OBSERVATION', 'PATIENT_STATUS', true);

-- ============================================================
-- USER SEED DATA: 5 Nurses + 5 Doctors
-- Password for all: Nurse@123 / Doctor@123
-- ============================================================

DELETE FROM role_access WHERE user_id IN (SELECT id FROM user WHERE username IN ('nurse1','nurse2','nurse3','nurse4','nurse5','doctor1','doctor2','doctor3','doctor4','doctor5'));
DELETE FROM user_details WHERE user_id IN (SELECT id FROM user WHERE username IN ('nurse1','nurse2','nurse3','nurse4','nurse5','doctor1','doctor2','doctor3','doctor4','doctor5'));
DELETE FROM user WHERE username IN ('nurse1','nurse2','nurse3','nurse4','nurse5','doctor1','doctor2','doctor3','doctor4','doctor5');

INSERT INTO user (username, password, user_type, active, created_by, created_on) VALUES
('nurse1',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NURSE',  true, 'seed', NOW()),
('nurse2',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NURSE',  true, 'seed', NOW()),
('nurse3',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NURSE',  true, 'seed', NOW()),
('nurse4',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NURSE',  true, 'seed', NOW()),
('nurse5',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'NURSE',  true, 'seed', NOW()),
('doctor1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', true, 'seed', NOW()),
('doctor2', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', true, 'seed', NOW()),
('doctor3', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', true, 'seed', NOW()),
('doctor4', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', true, 'seed', NOW()),
('doctor5', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'DOCTOR', true, 'seed', NOW());

INSERT INTO user_details (user_id, first_name, last_name, gender, dob, designation, email, phone_number, qualification, created_by, created_on) VALUES
((SELECT id FROM user WHERE username = 'nurse1'),  'Emily',    'Johnson',  'FEMALE', '1990-03-15', 'Senior Nurse',     'emily.johnson@eldercare.com',    '555-0101', 'BSN, RN',                       'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse2'),  'Sarah',    'Williams', 'FEMALE', '1992-07-22', 'Staff Nurse',      'sarah.williams@eldercare.com',   '555-0102', 'BSN, RN',                       'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse3'),  'Michael',  'Brown',    'MALE',   '1988-11-10', 'Charge Nurse',     'michael.brown@eldercare.com',    '555-0103', 'MSN, RN',                       'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse4'),  'Jessica',  'Davis',    'FEMALE', '1995-05-18', 'Staff Nurse',      'jessica.davis@eldercare.com',    '555-0104', 'BSN, RN',                       'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse5'),  'David',    'Miller',   'MALE',   '1991-09-25', 'Senior Nurse',     'david.miller@eldercare.com',     '555-0105', 'BSN, RN, CCRN',                 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor1'), 'Robert',   'Anderson', 'MALE',   '1975-02-14', 'Geriatrician',     'robert.anderson@eldercare.com',  '555-0201', 'MD, Board Certified Geriatrics', 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor2'), 'Jennifer', 'Taylor',   'FEMALE', '1980-06-08', 'Internal Medicine','jennifer.taylor@eldercare.com',  '555-0202', 'MD, FACP',                      'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor3'), 'William',  'Thomas',   'MALE',   '1978-12-20', 'Cardiologist',     'william.thomas@eldercare.com',   '555-0203', 'MD, FACC',                      'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor4'), 'Linda',    'Martinez', 'FEMALE', '1982-04-30', 'Neurologist',      'linda.martinez@eldercare.com',   '555-0204', 'MD, PhD',                       'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor5'), 'James',    'Garcia',   'MALE',   '1976-08-12', 'Geriatrician',     'james.garcia@eldercare.com',     '555-0205', 'MD, CMD',                       'seed', NOW());

INSERT INTO role_access (user_id, role_id, created_by, created_on) VALUES
((SELECT id FROM user WHERE username = 'nurse1'),  2, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse2'),  2, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse3'),  2, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse4'),  2, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'nurse5'),  2, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor1'), 3, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor2'), 3, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor3'), 3, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor4'), 3, 'seed', NOW()),
((SELECT id FROM user WHERE username = 'doctor5'), 3, 'seed', NOW());
