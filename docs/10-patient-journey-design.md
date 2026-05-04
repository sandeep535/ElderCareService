# Patient Journey — Completion Tracker Design

## Purpose

When a patient is registered, their record is not complete immediately. Staff may:
1. Register basic details today
2. Come back tomorrow to add medical info
3. Add admission details later

The `patient_journey` table tracks which sections have been filled so the UI can:
- Show **incomplete patients** in a "Pending" section for staff to continue editing
- Show **complete patients** in a "View Details" section
- Display a progress checklist per patient

---

## Sections Tracked

| Flag          | Set to true when...                              |
|---------------|--------------------------------------------------|
| basic_details | Patient row is first saved (`POST /api/patients`)|
| is_medical    | Medical section saved (`POST /api/patients/{id}/medical`) |
| is_admission  | Admission saved (`POST /api/patients/{id}/admission`) |
| is_note       | First note saved (`POST /api/patients/{id}/notes`) |

---

## Lifecycle

```
Staff registers patient
  → patient row saved
  → patient_journey row auto-created:
      basic_details = true
      is_medical    = false
      is_admission  = false
      is_note       = false

Staff saves medical info
  → medical row saved
  → patient_journey.is_medical = true

Staff saves admission
  → admission row saved
  → patient_journey.is_admission = true

Staff adds a note
  → note row saved
  → patient_journey.is_note = true

All flags = true → patient appears in "Complete" section
```

---

## isComplete Logic

```java
boolean isComplete = journey.isBasicDetails()
                  && journey.isMedical()
                  && journey.isAdmission()
                  && journey.isNote();
```

This is computed in the service layer — no extra DB column needed.

---

## Patient List — Two Sections

`GET /api/patients` returns all patients. Each patient in the response includes:

```json
{
  "id": 1,
  "patientId": "EC-2025-0001",
  "firstName": "John",
  "lastName": "Doe",
  "completionStatus": "INCOMPLETE",
  "pendingSections": ["MEDICAL", "ADMISSION"],
  "activeAlert": false
}
```

Frontend uses `completionStatus` to split into two sections:
- `COMPLETE` → "View Details" section
- `INCOMPLETE` → "Pending / Continue Editing" section

`pendingSections` tells the UI exactly which tabs still need data — so the edit button can deep-link to the right tab.

---

## API

| Method | Path                       | Role     | Description                    |
|--------|----------------------------|----------|--------------------------------|
| GET    | /api/patients/{id}/journey | Any auth | Get journey flags for a patient|

Response:
```json
{
  "patientId": 1,
  "basicDetails": true,
  "isMedical": false,
  "isAdmission": false,
  "isNote": false,
  "isComplete": false,
  "pendingSections": ["MEDICAL", "ADMISSION", "NOTES"]
}
```

---

## Notes on NOK

NOK is not tracked in `patient_journey` because:
- NOK is optional (not all patients have next of kin)
- It should not block a patient from appearing as "Complete"

If you want to track NOK in future, add `is_nok BOOLEAN` to `patient_journey` — zero impact on existing logic.

---

## Notes on Vitals, Medication, Clinical Notes

These are ongoing/recurring records (not one-time setup). They are not tracked in `patient_journey`.
Instead, the patient detail view shows counts:
```json
"vitalCount": 5,
"medicationCount": 3,
"clinicalNoteCount": 2
```
