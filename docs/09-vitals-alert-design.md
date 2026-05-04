# Vitals — Alert Design

## Purpose

Each vital reading is a time-series snapshot. When any reading crosses a clinical threshold, an alert is raised on that patient. The alert stays active until the **next reading** shows all values are back within normal range.

---

## Normal Ranges (Standard Clinical Thresholds)

| Vital      | Field      | Normal Range         | Alert if outside         |
|------------|------------|----------------------|--------------------------|
| Systolic BP| systolic   | 90 – 139 mmHg        | < 90 or > 139            |
| Diastolic BP| diastolic | 60 – 89 mmHg         | < 60 or > 89             |
| Heart Rate | hr         | 60 – 100 bpm         | < 60 or > 100            |
| Temperature| temp       | 97.0 – 99.5 °F       | < 97.0 or > 99.5         |
| SpO2       | spo2       | 95 – 100 %           | < 95                     |

> These thresholds will be configurable via `application.properties` so they can be adjusted without code change.

---

## Table Changes

Two columns added to `vital`:

| Column         | Type    | Notes                                          |
|----------------|---------|------------------------------------------------|
| has_alert      | BOOLEAN | true if any reading in this row crossed limit  |
| alert_resolved | BOOLEAN | true if this row brought all values to normal  |

---

## Alert Logic (Service Layer)

```
POST /api/patients/{id}/vitals  →  VitalService.save(request)

1. Save the vital row
2. Check each field against thresholds
3. If ANY field is out of range:
     vital.hasAlert = true
     vital.alertResolved = false
   Else:
     vital.hasAlert = false
     vital.alertResolved = true   ← this reading resolved the previous alert

4. Save updated vital row
```

---

## How the Alert Shows on the Patient List

The patient list API (`GET /api/patients`) returns a summary per patient.
The summary includes an `activeAlert` flag:

```
SELECT v.*
FROM vital v
WHERE v.patient_id = :patientId
ORDER BY v.created_on DESC
LIMIT 1
```

If the **latest** vital row has `has_alert = true` AND `alert_resolved = false` → `activeAlert = true` in the patient list response.

If the latest row has `alert_resolved = true` → alert is cleared, `activeAlert = false`.

---

## Patient List Response Shape (with alert)

```json
{
  "patientId": "EC-2025-0001",
  "firstName": "John",
  "lastName": "Doe",
  "activeAlert": true,
  "alertDetails": {
    "systolic": 155,
    "diastolic": 95,
    "hr": 110,
    "temp": 101.2,
    "spo2": 93,
    "recordedAt": "2025-07-01T22:30:00"
  }
}
```

---

## Two Patient Sections on Dashboard

Based on `patient_journey` (completion) and `vital.has_alert` (alert):

| Section                  | Condition                                                    |
|--------------------------|--------------------------------------------------------------|
| "Complete — View Details"| All `patient_journey` flags = true AND no active alert       |
| "Incomplete"             | Any `patient_journey` flag = false                           |
| "Alert" badge            | Latest vital has `has_alert = true` (shown on any patient)   |

These are not separate tables — the patient list API computes this from `patient_journey` + latest `vital` row and returns a `status` field:

```json
{
  "completionStatus": "COMPLETE",   // COMPLETE / INCOMPLETE
  "activeAlert": true
}
```

---

## Vital Thresholds in application.properties

```properties
vitals.threshold.systolic.min=90
vitals.threshold.systolic.max=139
vitals.threshold.diastolic.min=60
vitals.threshold.diastolic.max=89
vitals.threshold.hr.min=60
vitals.threshold.hr.max=100
vitals.threshold.temp.min=97.0
vitals.threshold.temp.max=99.5
vitals.threshold.spo2.min=95
```

A `VitalThresholdConfig` `@ConfigurationProperties` class will load these — no hardcoded numbers in service code.

---

## Open Question — OQ-11 (new)

**Should we store the threshold values in DB (master_table or a new config table) instead of application.properties?**

- `application.properties` → simpler, requires restart to change thresholds
- DB config table → admin can change thresholds at runtime via API

**Recommendation:** Start with `application.properties`. If clinical staff need to adjust thresholds per patient or per facility, move to DB config table later.
