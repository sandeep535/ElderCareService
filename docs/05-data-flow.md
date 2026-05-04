# Data Flow — Table by Table

## 1. user + user_details

**Purpose:** Authentication identity and staff profile.

**Flow:**
1. Admin calls `POST /api/users` with username, password, userType, and profile details.
2. Service hashes password with BCrypt, saves `user` row.
3. Saves `user_details` row linked by `user_id`.
4. Assigns role via `role_access`.

**Key rule:** `username` must be unique. Password never returned in any response.

---

## 2. role + role_access

**Purpose:** Define security roles and assign them to users.

**Flow:**
1. 5 standard roles seeded at startup. Admin can create additional roles via `POST /api/roles`.
2. Service auto-prepends `ROLE_` to the `role_id` input (e.g. input `PHARMACIST` → stored as `ROLE_PHARMACIST`).
3. When a user is created/updated, admin assigns one or more roles via `role_access`.
4. At login, `AppUserDetailsService` loads user → joins `role_access` → `role` → builds `GrantedAuthority` list.
5. JWT token carries roles as claims.
6. Role hierarchy in `SecurityConfig` determines inheritance — see `12-role-hierarchy-guide.md`.

---

## 3. patient

**Purpose:** Core patient identity record.

**Flow:**
1. Staff calls `POST /api/patients` with basic demographics.
2. Service generates a unique `patient_id` (e.g. EC-2025-0001).
3. A `patient_journey` row is created automatically with all flags = false.
4. `basic_details` flag on `patient_journey` is set to true.

---

## 4. nok (Next of Kin)

**Purpose:** Emergency contacts and medical decision makers for a patient.

**Flow:**
1. After patient is registered, staff calls `POST /api/patients/{id}/nok`.
2. Multiple NOK records can exist per patient.
3. Only one NOK should have `primary_contact = true` — service enforces this.
4. `can_make_medical = true` flags who can authorise medical decisions.

---

## 5. medical

**Purpose:** Clinical summary — physician, nurse, allergies, blood type, current meds.

**Flow:**
1. Staff calls `POST /api/patients/{id}/medical`.
2. One medical record per patient (upsert pattern).
3. On save, `patient_journey.is_medical` is set to true.
4. `audit_log` entry created with snapshot of data.

---

## 6. admission

**Purpose:** Tracks current/past admission — room, bed, status.

**Flow:**
1. Staff calls `POST /api/patients/{id}/admission`.
2. One active admission per patient at a time.
3. Status values from `master_table` (type = PATIENT_STATUS): e.g. ADMITTED, DISCHARGED.
4. On save, `patient_journey.is_admission` is set to true.

---

## 7. notes

**Purpose:** General free-text notes attached to a patient.

**Flow:**
1. Any authenticated staff calls `POST /api/patients/{id}/notes`.
2. Multiple notes per patient, append-only.
3. On first note, `patient_journey.is_note` is set to true.

---

## 8. address

**Purpose:** Polymorphic address store for patients, staff, NOK.

**Flow:**
1. When saving a patient/staff/NOK, address is saved separately via `POST /api/addresses`.
2. `type` = PATIENT / STAFF / NOK, `type_id` = the respective entity's id.
3. Fetched by `GET /api/addresses/{type}/{typeId}`.

---

## 9. patient_journey

**Purpose:** Completion tracker — shows which sections of a patient's record are filled.

**Flow:**
- Created automatically when patient is registered (all flags false).
- `basic_details` → true when patient saved.
- `is_medical` → true when medical section saved.
- `is_admission` → true when admission saved.
- `is_note` → true when first note saved.
- Frontend uses this to show progress/checklist UI.

---

## 10. diagnoses

**Purpose:** Patient-specific diagnosis records linked to ICD master.

**Flow:**
1. Staff calls `POST /api/patients/{id}/diagnoses`.
2. `name` can be free text or sourced from `diagnosis_master`.
3. `status` from `master_table`.
4. Multiple diagnoses per patient.

---

## 11. audit_log

**Purpose:** Immutable audit trail of audited patient data changes.

**Flow:**
- Service layer writes an `audit_log` entry asynchronously via `AuditService`.
- Currently, audit writes are invoked from medical save and vitals recording.
- `data_json` stores a JSON snapshot of the saved module payload.
- `type_screen` identifies which module triggered the audit.
- Planned future coverage includes admission, diagnoses, medication, clinical_note, and medical_history.
- No public audit read API is implemented yet; a read-only endpoint like `GET /api/patients/{id}/audit` is planned for ADMIN use.

---

## 12. vital

**Purpose:** Time-series vital signs readings.

**Flow:**
1. Nurse calls `POST /api/patients/{id}/vitals` after each observation.
2. Each call creates a new row (append-only).
3. `created_on` serves as the reading timestamp.
4. List endpoint returns all readings ordered by `created_on DESC`.

---

## 13. medication_master + patient_medication + medication_slot

**Purpose:** Full medicine tracking — catalogue, prescription, and per-dose slot scheduling.

**Flow:**
1. Admin seeds `medication_master` with available medicines (Dolo 650, Paracetamol, etc.).
2. Nurse prescribes via `POST /api/patients/{id}/medications` with frequency, duration, slot times.
3. Service creates a `patient_medication` row and bulk-generates `medication_slot` rows (frequency × duration_days).
4. Dashboard shows next-due slots via `GET /api/patients/{id}/medications/next-due`.
5. Nurse marks each slot GIVEN via `PUT /slots/{slotId}/given` — records `given_at` and `given_by`.
6. Hourly scheduler marks overdue PENDING slots as MISSED.
7. When all slots are GIVEN/MISSED/SKIPPED → `patient_medication.active = false`.

See `11-medication-tracking-design.md` for full detail.

---

## 14. clinical_note

**Purpose:** Structured clinical observations/notes with type, priority, and author.

**Flow:**
1. Any authenticated staff calls `POST /api/patients/{id}/clinical-notes`.
2. `notes_type` from `master_table` (type = NOTES_TYPE).
3. `recorded_by` = FK to `user.id` (auto-populated from JWT context).
4. `priority` = HIGH / MEDIUM / LOW.

---

## 15. medical_history

**Purpose:** Past surgical/medical history for a patient.

**Flow:**
1. Staff calls `POST /api/patients/{id}/medical-history`.
2. `type` from `master_table` (type = SURGERY_TYPE).
3. `procedure_code` = optional ICD/CPT code.

---

## 16. master_table

**Purpose:** Centralised lookup/dropdown values.

**Flow:**
- Seeded at DB setup.
- Frontend calls `GET /api/masters/{type}` to populate dropdowns.
- Admin can add/update entries.
- Types: GENDER, BLOOD_TYPE, RELATIONSHIP, PATIENT_STATUS, NOTES_TYPE, SURGERY_TYPE.

---

## 17. diagnosis_master

**Purpose:** ICD code catalogue for diagnosis autocomplete.

**Flow:**
- Seeded with ICD-10 codes.
- Staff searches via `GET /api/diagnosis-master?search=diabetes`.
- `active = false` soft-deletes an entry.

---

## 18. invalidated_token

**Purpose:** JWT logout blacklist.

**Flow:**
1. On `POST /api/auth/logout`, token is saved here.
2. `JwtFilter` checks this table on every request.
3. Scheduled cleanup job deletes expired tokens daily.
