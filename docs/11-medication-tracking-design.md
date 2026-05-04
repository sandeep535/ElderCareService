# Medication Tracking — Design

## Purpose

Track every medicine prescribed to a patient with:
- What medicine, what dose, how many times a day, for how many days
- Individual time slots generated automatically per dose
- Nurse marks each slot as GIVEN / MISSED / SKIPPED
- System shows "next due" medicine for a patient at any time

---

## Three Tables

```
medication_master          patient_medication          medication_slot
─────────────────          ──────────────────          ───────────────
id                    ←──  medication_id               id
name                       id                     ←──  patient_medication_id
generic_name               patient_id ──────────────── patient_id
strength                   dose                        scheduled_time
form                       frequency (2x/day)          status (PENDING/GIVEN/MISSED)
active                     duration_days (5 days)      given_at
                           start_date                  given_by (user FK)
                           end_date                    notes
                           instructions
                           active
```

---

## How Slots Are Generated

When a nurse prescribes a medicine via `POST /api/patients/{id}/medications`:

**Example:** Dolo 650 — 2 times a day — for 5 days — starting 2025-07-01 — slots: MORNING (08:00) and NIGHT (22:00)

```
frequency = 2, duration_days = 5, start_date = 2025-07-01
slotCodes = [MORNING, NIGHT]
Service reads master_table: MORNING → hour=8, NIGHT → hour=22

Generated slots (2 × 5 = 10 rows in medication_slot):
  2025-07-01 08:00  PENDING
  2025-07-01 22:00  PENDING
  2025-07-02 08:00  PENDING
  2025-07-02 22:00  PENDING
  ...
  2025-07-05 22:00  PENDING
```

---

## Slot Times — Driven by master_table

Slot times are **not hardcoded**. They come from `master_table` where `type = MEDICATION_SLOT_TIME`.

| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 8            | Morning     | MORNING     |
| 13           | Afternoon   | AFTERNOON   |
| 18           | Evening     | EVENING     |
| 22           | Night       | NIGHT       |

`lookup_value` = the actual hour (24h format) used to build `scheduled_time`.
Admin changes Morning from 8 to 7 via `PUT /api/masters/{id}` — zero code change.

---

## Slot Times Input

The prescription request includes `slotCodes` — a list of `lookup_code` values from `MEDICATION_SLOT_TIME`, matching `frequency`:

```json
{
  "medicationId": 5,
  "dose": "1 tablet",
  "frequency": 2,
  "durationDays": 5,
  "startDate": "2025-07-01",
  "instructions": "After food",
  "slotCodes": ["MORNING", "NIGHT"]
}
```

Service resolves `MORNING` → `lookup_value = 8` → schedules at 08:00.
Service resolves `NIGHT` → `lookup_value = 22` → schedules at 22:00.

Validation: `slotCodes.size()` must equal `frequency`.

---

## Slot Status Values

| Status  | Meaning                                      |
|---------|----------------------------------------------|
| PENDING | Dose is due, not yet given                   |
| GIVEN   | Nurse administered the dose, `given_at` set  |
| MISSED  | Time passed without being given              |
| SKIPPED | Intentionally skipped with a reason in notes |

---

## "Next Due" Medicine API

`GET /api/patients/{id}/medications/next-due`

Returns all PENDING slots where `scheduled_time <= now + 1 hour` (configurable window):

```json
[
  {
    "medicationName": "Dolo 650",
    "dose": "1 tablet",
    "instructions": "After food",
    "scheduledTime": "2025-07-01T20:00:00",
    "slotId": 8,
    "status": "PENDING"
  }
]
```

Frontend shows this as the "Next Medicine Due" panel for the patient.

---

## Marking a Slot as Given

`PUT /api/patients/{id}/medications/slots/{slotId}/given`

```json
{
  "givenAt": "2025-07-01T20:05:00",
  "notes": "Patient took without issues"
}
```

Service sets `status = GIVEN`, `given_at`, `given_by` = logged-in user from JWT context.

---

## Missed Slot Detection

A scheduled job runs every hour:
```
SELECT * FROM medication_slot
WHERE status = 'PENDING'
AND scheduled_time < NOW()
```
Marks overdue PENDING slots as MISSED automatically.

---

## Full Medication APIs

| Method | Path                                              | Role        | Description                    |
|--------|---------------------------------------------------|-------------|--------------------------------|
| GET    | /api/medication-master                            | Any auth    | Search medicine catalogue      |
| POST   | /api/medication-master                            | ADMIN       | Add medicine to catalogue      |
| PUT    | /api/medication-master/{id}                       | ADMIN       | Update medicine                |
| POST   | /api/patients/{id}/medications                    | ADMIN,NURSE | Prescribe medicine + gen slots |
| GET    | /api/patients/{id}/medications                    | Any auth    | List active prescriptions      |
| PUT    | /api/patients/{id}/medications/{pmId}/stop        | ADMIN,NURSE | Stop a prescription            |
| GET    | /api/patients/{id}/medications/next-due           | Any auth    | Next due slots                 |
| GET    | /api/patients/{id}/medications/slots              | Any auth    | All slots (filterable by date) |
| PUT    | /api/patients/{id}/medications/slots/{slotId}/given   | NURSE,ADMIN | Mark as given              |
| PUT    | /api/patients/{id}/medications/slots/{slotId}/skipped | NURSE,ADMIN | Mark as skipped            |

---

## Data Flow Summary

```
1. Admin seeds medication_master (Dolo 650, Paracetamol, etc.)
2. Nurse prescribes → POST /patients/{id}/medications
   → patient_medication row created
   → medication_slot rows bulk-generated (frequency × duration_days)
3. Dashboard shows next-due slots per patient
4. Nurse gives medicine → PUT /slots/{id}/given
5. Scheduler marks overdue slots as MISSED
6. When all slots for a prescription are GIVEN/MISSED/SKIPPED
   → patient_medication.active = false (course complete)
```

---

## medical.current_medication (free text) vs patient_medication

| Field | Purpose |
|-------|---------|
| `medical.current_medication` | Free-text summary of ongoing medications at admission time — e.g. "Patient is on blood thinners" |
| `patient_medication` | Structured, trackable prescriptions with slot scheduling |

Both are maintained. They serve different purposes — one is a clinical summary note, the other is the active tracking system.
