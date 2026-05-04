# Audit Log — Purpose, Design & Usage

## Current Implementation Status

The audit write path is partially implemented.
- `AuditService.record(...)` is called asynchronously from `MedicalService.save` and `VitalService.record`.
- `AuditLogEntity` rows are persisted in the background, and failures are written to `AuditFailureEntity`.
- Public audit read APIs are planned but not yet implemented.
- Planned future coverage includes admission, diagnoses, medication, clinical_note, and medical_history.

---

## Purpose

Every patient data save triggers an audit entry showing **what action was taken, on which patient, with what data**.
The UI will use this to show a "Recent Activity" timeline per patient — ordered by most recent first.

---

## How the Audit Flow Works

The audit write is **async** — it does NOT block the main save operation.

```
POST /api/patients/{id}/medical   (main request)
  │
  ├─→ MedicalService.save(request)
  │     → medicalRepo.save(entity)          ← main save (sync)
  │     → auditService.record(              ← fire and forget (async)
  │           patientId   = actual patient id
  │           typeScreen  = "MEDICAL"
  │           data        = saved medical response payload
  │       )
  │
  └─→ Response returned to caller immediately
        (audit write happens in background)
```

The caller gets their response without waiting for the audit write.

---

## What Goes in data_json

Only the data that was saved in **that specific module** — not the entire patient record.

| typeScreen      | data_json contains                          |
|-----------------|---------------------------------------------|
| PATIENT         | patient row fields                          |
| MEDICAL         | medical row fields only                     |
| ADMISSION       | admission row fields only                   |
| NOK             | the specific nok row saved                  |
| DIAGNOSES       | the specific diagnosis row saved            |
| VITALS          | the vital reading row                       |
| MEDICATION      | patient_medication row (not slots)          |
| CLINICAL_NOTE   | clinical_note row                           |
| MEDICAL_HISTORY | medical_history row                         |
| NOTES           | the note row saved                          |

Example — when medical is saved, `data_json`:
```json
{
  "primaryPhysician": "Dr. Smith",
  "nurse": "Jane",
  "allergies": "Penicillin",
  "bloodType": "A_POS",
  "capturedBy": "sandeep535",
  "savedAt": "2025-07-01T10:30:00"
}
```

---

## audit_log Table

| Column      | Type         | Notes                                         |
|-------------|--------------|-----------------------------------------------|
| id          | BIGINT PK AI |                                               |
| created_on  | DATETIME     | Timestamp of the action                       |
| type_screen | VARCHAR(100) | Module that triggered this (MEDICAL, VITALS…) |
| data_json   | LONGTEXT     | JSON of only the data saved in that module    |
| patient_id  | BIGINT        | Stored patient id                             |

> No audit columns on this table — it IS the audit record.

---

## audit_failure Table (NEW)

When async audit persistence fails for any reason, the failure is captured here so no data is lost.

| Column        | Type         | Notes                                          |
|---------------|--------------|------------------------------------------------|
| id            | BIGINT PK AI |                                                |
| created_on    | DATETIME     | When the failure occurred                      |
| type_screen   | VARCHAR(100) | Which module triggered the audit               |
| data_json     | LONGTEXT     | The same payload that failed to save           |
| patient_id    | BIGINT        | Patient id captured for troubleshooting        |
| failure_reason| TEXT         | Exception message or persistence failure detail |
| retry_count   | INT          | How many times retry was attempted             |
| resolved      | BOOLEAN      | true once manually or auto-reprocessed         |

**Why store `patient_id` as plain BIGINT (not FK)?**
If the failure is severe enough that the patient record itself is in question, a hard FK would prevent inserting the failure row. Plain BIGINT always allows the failure to be recorded.

---

## Async Flow Detail

```
auditService.record(patientId, typeScreen, data)
  → async repository save to audit_log
  → if save fails
      → catch exception
      → insert row into audit_failure table with failure_reason
      → log error via Log4j2
```

---

## Kafka-Ready Design (Future)

The async persistence layer is intentionally isolated in `AuditService`. When you are ready to move to Kafka:

```
Current:
  auditService.record() → async repository save → audit_log

Future (zero change to callers):
  auditService.record() → Kafka producer → topic: audit-events
                                              → Kafka consumer → audit_log
```

Only `AuditService` changes. Every service that calls `auditService.record()` stays exactly the same.

The `audit_failure` table also maps cleanly to a Kafka dead-letter queue (DLQ) concept — failed events land there for reprocessing.

---

## UI — Recent Activity Timeline

`GET /api/patients/{id}/audit` is planned to return entries ordered by `created_on DESC`:

```json
[
  {
    "typeScreen": "VITALS",
    "createdOn": "2025-07-01T22:30:00",
    "dataJson": { "systolic": 155, "hr": 110, "hasAlert": true }
  },
  {
    "typeScreen": "MEDICAL",
    "createdOn": "2025-07-01T10:00:00",
    "dataJson": { "bloodType": "A_POS", "allergies": "Penicillin" }
  }
]
```

Frontend will render this as a timeline.

---

## APIs

| Method | Path                                  | Role  | Description                     |
|--------|---------------------------------------|-------|---------------------------------|
| GET    | /api/patients/{id}/audit              | ADMIN | Planned activity timeline       |
| GET    | /api/patients/{id}/audit?type=VITALS  | ADMIN | Planned filter by module        |
| GET    | /api/audit/failures                   | ADMIN | Planned view of failed writes   |
| PUT    | /api/audit/failures/{id}/resolve      | ADMIN | Planned resolve failure entry   |

---

## Rules

1. `audit_log` rows are never updated or deleted.
2. `audit_failure` rows are never deleted — only marked `resolved = true`.
3. The main save never fails because of an audit failure — they are fully decoupled.
4. `captured_by` is embedded inside `data_json`, not a separate column.
