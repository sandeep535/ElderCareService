# Table Clarity Check — Full Review

## master_table — CONFIRMED UNDERSTANDING

The single source of truth for ALL configurable dropdown/lookup values across the entire application.

`lookup_value` = the **actual usable value** (not just a sort order).
- For GENDER: `lookup_value = 1` is just a key
- For MEDICATION_SLOT_TIME: `lookup_value = 8` means **8 AM** — the service reads this number directly to build `scheduled_time`

Every field in every table that says "FK → master_table" stores the `lookup_code` (e.g. `FEMALE`, `MORNING`, `ADMITTED`).
The display label (`lookup_item`) is resolved at response time by joining master_table.

**All types confirmed:**

| Type                  | Used by                                         |
|-----------------------|-------------------------------------------------|
| GENDER                | patient.gender, nok.gender, user_details.gender |
| BLOOD_TYPE            | medical.blood_type                              |
| RELATIONSHIP          | nok.relationship                                |
| PATIENT_STATUS        | admission.status                                |
| NOTES_TYPE            | clinical_note.notes_type                        |
| SURGERY_TYPE          | medical_history.type                            |
| MEDICATION_SLOT_TIME  | medication_slot generation (lookup_value = hour)|
| DIAGNOSIS_STATUS      | diagnoses.status                                |
| CLINICAL_NOTE_PRIORITY| clinical_note.priority                          |

---

## user + user_details — CONFIRMED

- `user` = login credentials only (username, bcrypt password, user_type)
- `user_details` = profile (name, gender, dob, email, phone, designation, qualification)
- `user_type` = display label (e.g. "Senior Nurse") — NOT the security role
- Security role comes from `role_access` → `role`
- One user can have multiple roles

---

## role + role_access — CONFIRMED

- `role` table = catalogue of roles. 5 seeded. Admin creates more via API.
- Service auto-prepends `ROLE_` to `role_id` input
- `role_access` = bridge table linking user → role (many-to-many)
- At login: user's roles loaded → `GrantedAuthority` list → JWT claims
- Hierarchy in `SecurityConfig.java` — see `12-role-hierarchy-guide.md`

---

## patient — CONFIRMED

- Core identity: name, dob, gender, system-generated `patient_id` (EC-2025-0001)
- On save: `patient_journey` row auto-created with all flags = false, `basic_details` = true

---

## nok — CONFIRMED

- Multiple NOK per patient
- `primary_contact = true` → only one allowed (service enforces)
- `can_make_medical = true` → authorised for medical decisions
- NOT tracked in `patient_journey` (optional section)

---

## medical — CONFIRMED

- One row per patient (upsert)
- `current_medication` = free-text summary (e.g. "Patient is on blood thinners since 2020")
- `blood_type` → `master_table` (BLOOD_TYPE)
- On save: `patient_journey.is_medical = true`

---

## admission — CONFIRMED

- One active admission per patient
- `status` → `master_table` (PATIENT_STATUS): ADMITTED, DISCHARGED, ON_LEAVE, CRITICAL, STABLE
- On save: `patient_journey.is_admission = true`

---

## notes — CONFIRMED

- General free-text notes, multiple per patient, append-only
- On first note: `patient_journey.is_note = true`

---

## address — CONFIRMED

- Single polymorphic table for patient / staff / NOK addresses
- `type` = PATIENT / STAFF / NOK
- `type_id` = the id of the respective entity
- Query always: `WHERE type = ? AND type_id = ?`

---

## patient_journey — CONFIRMED

- Auto-created when patient registered
- Flags flipped by service layer as each section is saved
- `isComplete` = all 4 flags true → patient moves to "View Details" section
- `pendingSections` list computed in service → tells frontend which tabs still need data

---

## diagnoses — CONFIRMED

- Multiple diagnoses per patient
- `name` = free text or sourced from `diagnosis_master` (ICD catalogue)
- `status` → `master_table` (DIAGNOSIS_STATUS): ACTIVE, RESOLVED, MONITORING, RULED_OUT
- `diagnosis_by` = free text name of diagnosing doctor

---

## audit_log + audit_failure — CONFIRMED

- Async flow: main save completes first, audit write happens in background (fire and forget)
- `data_json` = only the data saved in that specific module (not full patient record)
- `type_screen` = module name (MEDICAL, VITALS, ADMISSION, etc.)
- UI uses audit trail as "Recent Activity" timeline ordered by `created_on DESC`
- `audit_failure` table captures any failed async audit writes:
  - `failure_reason` = exception/HTTP error message
  - `patient_id` = plain BIGINT (not FK) so failure row always inserts
  - `resolved` flag for reprocessing
- Kafka-ready: only `AuditService.recordAsync()` changes when migrating to event-driven — all callers stay the same
- See `08-audit-log-design.md` for full detail

---

## vital — CONFIRMED

- Time-series, one row per reading (append-only)
- `has_alert` = true if any reading crossed threshold
- `alert_resolved` = true if this reading brought all values back to normal
- Latest row drives the alert badge on patient list
- Thresholds in `application.properties` via `@ConfigurationProperties`

---

## medication_master — CONFIRMED

- Catalogue of all available medicines, static data
- Admin-managed, reusable across patients
- `form` = free text (Tablet, Syrup, Injection, Capsule) — no link to master_table
- No FK to master_table — standalone catalogue table

---

## patient_medication — CONFIRMED

- Links a medicine to a patient with full prescription details
- `frequency` = times per day (e.g. 2)
- `duration_days` = total days (e.g. 5)
- `end_date` = computed by service: `start_date + duration_days`
- `active = false` when course completed or stopped

---

## medication_slot — CONFIRMED

- One row per individual dose
- `scheduled_time` = computed from `start_date` + day offset + hour from `master_table (MEDICATION_SLOT_TIME)`
- `status`: PENDING → GIVEN / MISSED / SKIPPED
- `given_by` = FK to `user.id` (nurse who administered)
- Hourly scheduler marks overdue PENDING → MISSED

**Slot generation flow:**
```
slotCodes = [MORNING, NIGHT]
Service reads master_table WHERE type='MEDICATION_SLOT_TIME' AND lookup_code IN ('MORNING','NIGHT')
→ MORNING.lookup_value = 8  → hour = 8
→ NIGHT.lookup_value   = 22 → hour = 22

For each day (1 to duration_days):
  For each slot hour:
    INSERT medication_slot (scheduled_time = start_date + day + hour, status = PENDING)
```

---

## clinical_note — CONFIRMED

- Structured notes with type, priority, title, author
- `notes_type` → `master_table` (NOTES_TYPE)
- `priority` → `master_table` (CLINICAL_NOTE_PRIORITY): HIGH, MEDIUM, LOW
- `recorded_by` → `user.id` (auto from JWT context, not user input)

---

## medical_history — CONFIRMED

- Past surgical/medical history
- `type` → `master_table` (SURGERY_TYPE)
- `procedure_code` = optional ICD/CPT code

---

## diagnosis_master — CONFIRMED

- ICD-10 code catalogue
- Seeded with standard codes
- `active = false` = soft delete
- Used for autocomplete when adding a diagnosis

---

## invalidated_token — CONFIRMED

- JWT logout blacklist
- Checked on every request in `JwtFilter`
- Cleaned up daily by scheduler

---

## Tables Count Summary

| #  | Table                  | Status    |
|----|------------------------|-----------|
| 1  | user                   | ✅ Clear  |
| 2  | user_details           | ✅ Clear  |
| 3  | role                   | ✅ Clear  |
| 4  | role_access            | ✅ Clear  |
| 5  | patient                | ✅ Clear  |
| 6  | nok                    | ✅ Clear  |
| 7  | medical                | ✅ Clear  |
| 8  | admission              | ✅ Clear  |
| 9  | notes                  | ✅ Clear  |
| 10 | address                | ✅ Clear  |
| 11 | patient_journey        | ✅ Clear  |
| 12 | diagnoses              | ✅ Clear  |
| 13 | audit_log              | ✅ Clear  |
| 14 | audit_failure          | ✅ Clear  |
| 15 | vital                  | ✅ Clear  |
| 16 | medication_master      | ✅ Clear  |
| 17 | patient_medication     | ✅ Clear  |
| 18 | medication_slot        | ✅ Clear  |
| 19 | clinical_note          | ✅ Clear  |
| 20 | medical_history        | ✅ Clear  |
| 21 | master_table           | ✅ Clear  |
| 22 | diagnosis_master       | ✅ Clear  |
| 23 | invalidated_token      | ✅ Clear  |

**All 23 tables confirmed. No pending questions. Ready for implementation.**
