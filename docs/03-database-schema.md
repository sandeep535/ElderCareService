# Database Schema — ElderCareDB

All tables include audit columns unless noted:
`created_by VARCHAR(100)`, `created_on DATETIME`, `updated_on DATETIME`, `updated_by VARCHAR(100)`

---

## user

| Column     | Type         | Notes                        |
|------------|--------------|------------------------------|
| id         | BIGINT PK AI |                              |
| username   | VARCHAR(100) | UNIQUE, NOT NULL             |
| password   | VARCHAR(255) | BCrypt encrypted             |
| user_type  | VARCHAR(50)  | e.g. Nurse, Doctor           |
| + audit cols |            |                              |

---

## user_details

| Column        | Type         | Notes              |
|---------------|--------------|--------------------|
| id            | BIGINT PK AI |                    |
| first_name    | VARCHAR(100) |                    |
| last_name     | VARCHAR(100) |                    |
| gender        | VARCHAR(20)  | FK → master_table  |
| dob           | DATE         |                    |
| designation   | VARCHAR(100) |                    |
| email         | VARCHAR(150) |                    |
| phone_number  | VARCHAR(20)  |                    |
| qualification | VARCHAR(200) |                    |
| user_id       | BIGINT FK    | → user.id          |
| + audit cols  |              |                    |

---

## role

| Column    | Type         | Notes                  |
|-----------|--------------|------------------------|
| id        | BIGINT PK AI |                        |
| role_name | VARCHAR(100) | e.g. Nurse             |
| role_id   | VARCHAR(50)  | e.g. ROLE_NURSE UNIQUE |
| + audit cols |           |                        |

**Seed data (standard roles):**

| id | role_name    | role_id             |
|----|--------------|---------------------|
| 1  | Admin        | ROLE_ADMIN          |
| 2  | Nurse        | ROLE_NURSE          |
| 3  | Doctor       | ROLE_DOCTOR         |
| 4  | Receptionist | ROLE_RECEPTIONIST   |
| 5  | Super Admin  | ROLE_SUPER_ADMIN    |

---

## role_access

| Column   | Type         | Notes           |
|----------|--------------|-----------------|
| id       | BIGINT PK AI |                 |
| role_id  | BIGINT FK    | → role.id       |
| user_id  | BIGINT FK    | → user.id       |
| + audit cols |          |                 |

---

## patient

| Column     | Type         | Notes                    |
|------------|--------------|--------------------------|
| id         | BIGINT PK AI |                          |
| first_name | VARCHAR(100) |                          |
| last_name  | VARCHAR(100) |                          |
| dob        | DATE         |                          |
| gender     | VARCHAR(20)  | FK → master_table        |
| patient_id | VARCHAR(50)  | UNIQUE, system-generated |
| + audit cols |            |                          |

---

## nok (Next of Kin)

| Column           | Type         | Notes                  |
|------------------|--------------|------------------------|
| id               | BIGINT PK AI |                        |
| first_name       | VARCHAR(100) |                        |
| last_name        | VARCHAR(100) |                        |
| relationship     | VARCHAR(50)  | FK → master_table      |
| dob              | DATE         |                        |
| gender           | VARCHAR(20)  | FK → master_table      |
| phone_number     | VARCHAR(20)  |                        |
| email            | VARCHAR(150) |                        |
| primary_contact  | BOOLEAN      |                        |
| can_make_medical | BOOLEAN      | Medical decision maker |
| notes            | TEXT         |                        |
| patient_id       | BIGINT FK    | → patient.id           |
| + audit cols     |              |                        |

---

## medical

| Column              | Type         | Notes             |
|---------------------|--------------|-------------------|
| id                  | BIGINT PK AI |                   |
| primary_physician   | VARCHAR(150) |                   |
| nurse               | VARCHAR(150) |                   |
| allergies           | TEXT         |                   |
| medical_alerts      | TEXT         |                   |
| current_medication  | TEXT         | Summary/free text |
| blood_type          | VARCHAR(10)  | FK → master_table |
| patient_id          | BIGINT FK    | → patient.id      |
| + audit cols        |              |                   |

---

## admission

| Column          | Type         | Notes             |
|-----------------|--------------|-------------------|
| id              | BIGINT PK AI |                   |
| admission_date  | DATE         |                   |
| room_number     | VARCHAR(20)  |                   |
| bed             | VARCHAR(20)  |                   |
| status          | VARCHAR(30)  | FK → master_table |
| emr_contact_name| VARCHAR(150) |                   |
| phone_number    | VARCHAR(20)  |                   |
| patient_id      | BIGINT FK    | → patient.id      |
| + audit cols    |              |                   |

---

## notes

| Column     | Type         | Notes         |
|------------|--------------|---------------|
| id         | BIGINT PK AI |               |
| notes      | TEXT         |               |
| patient_id | BIGINT FK    | → patient.id  |
| + audit cols |            |               |

---

## address

| Column   | Type         | Notes                              |
|----------|--------------|------------------------------------|
| id       | BIGINT PK AI |                                    |
| address  | VARCHAR(255) |                                    |
| city     | VARCHAR(100) |                                    |
| state    | VARCHAR(100) |                                    |
| zip_code | VARCHAR(20)  |                                    |
| type     | VARCHAR(30)  | PATIENT / STAFF / NOK              |
| type_id  | BIGINT       | FK value for the corresponding type|
| + audit cols |          |                                    |

---

## patient_journey

| Column        | Type         | Notes                          |
|---------------|--------------|--------------------------------|
| id            | BIGINT PK AI |                                |
| patient_id    | BIGINT FK    | → patient.id                   |
| basic_details | BOOLEAN      | Patient record filled          |
| is_medical    | BOOLEAN      | Medical section filled         |
| is_admission  | BOOLEAN      | Admission section filled       |
| is_note       | BOOLEAN      | Notes section filled           |
| + audit cols  |              |                                |

---

## diagnoses

| Column       | Type         | Notes                  |
|--------------|--------------|------------------------|
| id           | BIGINT PK AI |                        |
| name         | VARCHAR(200) |                        |
| date         | DATE         |                        |
| status       | VARCHAR(50)  | FK → master_table      |
| diagnosis_by | VARCHAR(150) |                        |
| notes        | TEXT         |                        |
| patient_id   | BIGINT FK    | → patient.id           |
| + audit cols |              |                        |

---

## audit_log

> No audit columns (this IS the audit table)

| Column      | Type         | Notes                                         |
|-------------|--------------|-----------------------------------------------|
| id          | BIGINT PK AI |                                               |
| created_on  | DATETIME     | Timestamp of the action                       |
| type_screen | VARCHAR(100) | Module that triggered this                    |
| data_json   | LONGTEXT     | JSON of only the data saved in that module    |
| patient_id  | BIGINT FK    | → patient.id                                  |

---

## audit_failure

> Captures failed async audit writes. Never deleted, only resolved.

| Column         | Type         | Notes                                              |
|----------------|--------------|----------------------------------------------------|
| id             | BIGINT PK AI |                                                    |
| created_on     | DATETIME     | When the failure occurred                          |
| type_screen    | VARCHAR(100) | Which module triggered the audit                   |
| data_json      | LONGTEXT     | Same payload that failed to save                   |
| patient_id     | BIGINT       | Plain BIGINT (not FK) — always insertable          |
| failure_reason | TEXT         | Exception message / HTTP error                     |
| retry_count    | INT          | default 0                                          |
| resolved       | BOOLEAN      | default false, true once reprocessed               |

---

## vital

| Column        | Type         | Notes                              |
|---------------|--------------|------------------------------------|n| id            | BIGINT PK AI |                                    |
| systolic      | INT          |                                    |
| diastolic     | INT          |                                    |
| hr            | INT          | Heart rate (bpm)                   |
| temp          | DECIMAL(4,1) | Temperature (°F or °C)             |
| spo2          | INT          | Oxygen saturation %                |
| notes         | TEXT         |                                    |
| has_alert     | BOOLEAN      | true if any reading crossed limit  |
| alert_resolved| BOOLEAN      | true when next normal reading saved|
| patient_id    | BIGINT FK    | → patient.id                       |
| + audit cols  |              | created_on = reading timestamp     |

---

## medication_master

Catalogue of all medicines. Reusable across patients. No link to master_table.

| Column       | Type         | Notes                          |
|--------------|--------------|--------------------------------|
| id           | BIGINT PK AI |                                |
| name         | VARCHAR(200) | Medicine name e.g. Dolo 650    |
| generic_name | VARCHAR(200) | Generic/chemical name          |
| strength     | VARCHAR(50)  | e.g. 650mg                     |
| form         | VARCHAR(50)  | Free text: Tablet/Syrup/etc.   |
| active       | BOOLEAN      | default true                   |
| + audit cols |              |                                |

---

## patient_medication

Links a medicine to a patient with dosage schedule and tracks slots.

| Column             | Type         | Notes                                      |
|--------------------|--------------|--------------------------------------------|
| id                 | BIGINT PK AI |                                            |
| patient_id         | BIGINT FK    | → patient.id                               |
| medication_id      | BIGINT FK    | → medication_master.id                     |
| dose               | VARCHAR(100) | e.g. 1 tablet                              |
| frequency          | INT          | Times per day e.g. 2                       |
| duration_days      | INT          | Total days e.g. 5                          |
| start_date         | DATE         |                                            |
| end_date           | DATE         | Computed: start_date + duration_days       |
| instructions       | VARCHAR(200) | e.g. After food                            |
| active             | BOOLEAN      | false when course completed/stopped        |
| + audit cols       |              |                                            |

---

## medication_slot

Individual scheduled dose slots generated from patient_medication.
One row per dose per day.

| Column              | Type         | Notes                                         |
|---------------------|--------------|-----------------------------------------------|
| id                  | BIGINT PK AI |                                               |
| patient_medication_id| BIGINT FK   | → patient_medication.id                       |
| patient_id          | BIGINT FK    | → patient.id (for easy querying)              |
| scheduled_time      | DATETIME     | Exact date+time this dose is due              |
| status              | VARCHAR(20)  | PENDING / GIVEN / MISSED / SKIPPED            |
| given_at            | DATETIME     | Actual time dose was administered             |
| given_by            | BIGINT FK    | → user.id (nurse who gave it)                 |
| notes               | TEXT         | Optional notes on this slot                   |
| + audit cols        |              |                                               |

---

## clinical_note

| Column      | Type         | Notes             |
|-------------|--------------|-------------------|
| id          | BIGINT PK AI |                   |
| notes_type  | VARCHAR(50)  | FK → master_table (NOTES_TYPE)          |
| date_time   | DATETIME     |                                         |
| title       | VARCHAR(200) |                                         |
| notes       | TEXT         |                                         |
| recorded_by | BIGINT FK    | → user.id                               |
| priority    | VARCHAR(20)  | FK → master_table (CLINICAL_NOTE_PRIORITY)|
| patient_id  | BIGINT FK    | → patient.id                            |
| + audit cols |             |                   |

---

## medical_history

| Column           | Type         | Notes             |
|------------------|--------------|-------------------|
| id               | BIGINT PK AI |                   |
| name             | VARCHAR(200) |                   |
| date             | DATE         |                   |
| type             | VARCHAR(50)  | FK → master_table |
| surgeon          | VARCHAR(150) |                   |
| hospital_facility| VARCHAR(200) |                   |
| procedure_code   | VARCHAR(50)  |                   |
| notes            | TEXT         |                   |
| patient_id       | BIGINT FK    | → patient.id      |
| + audit cols     |              |                   |

---

## master_table

| Column       | Type         | Notes                                    |
|--------------|--------------|------------------------------------------|
| id           | BIGINT PK AI |                                          |
| lookup_value | VARCHAR(10)  | Numeric or short key                     |
| lookup_item  | VARCHAR(100) | Display label (e.g. Female)              |
| lookup_code  | VARCHAR(50)  | Code (e.g. FEMALE)                       |
| type         | VARCHAR(50)  | GENDER / BLOOD_TYPE / RELATIONSHIP / ... |
| + audit cols |              |                                          |

Master types:
1. GENDER
2. BLOOD_TYPE
3. RELATIONSHIP
4. PATIENT_STATUS
5. NOTES_TYPE
6. SURGERY_TYPE
7. MEDICATION_SLOT_TIME
8. DIAGNOSIS_STATUS
9. CLINICAL_NOTE_PRIORITY

---

## diagnosis_master

| Column         | Type         | Notes          |
|----------------|--------------|----------------|
| id             | BIGINT PK AI |                |
| diagnosis_name | VARCHAR(200) |                |
| icd_code       | VARCHAR(20)  |                |
| active         | BOOLEAN      | default true   |
| + audit cols   |              |                |

---

## invalidated_token

| Column      | Type         | Notes                    |
|-------------|--------------|--------------------------|
| id          | BIGINT PK AI |                          |
| token       | TEXT         |                          |
| invalidated_at | DATETIME  |                          |
