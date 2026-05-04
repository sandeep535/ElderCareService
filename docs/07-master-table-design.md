# MasterTable — Purpose, Design & Usage

## Why MasterTable Exists

Every application has dropdown values — Gender, Blood Type, Relationship, Status etc.
Without a master table, these values are hardcoded in the frontend or scattered as enums in the backend.

**Problems with hardcoding:**
- Adding a new blood type means a code deployment
- Frontend and backend can go out of sync
- No way to deactivate a value without code change

**MasterTable solves this:** All dropdown values live in one DB table. Frontend always fetches from API. Admin can add/update values without any code change.

---

## Table Structure

| Column       | Example Value | Purpose                                      |
|--------------|---------------|----------------------------------------------|
| id           | 1             | PK                                           |
| lookup_value | "1"           | Short key / sort order                       |
| lookup_item  | "Female"      | Display label shown in UI dropdown           |
| lookup_code  | "FEMALE"      | Code used in backend logic and DB storage    |
| type         | "GENDER"      | Groups related values together               |

**Rule:** `lookup_code` is unique within a `type`. Queries always filter by `type`.

---

## All Types and Their Seed Data

### GENDER
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | Female      | FEMALE      |
| 2            | Male        | MALE        |
| 3            | Other       | OTHER       |

### BLOOD_TYPE
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | A+          | A_POS       |
| 2            | A-          | A_NEG       |
| 3            | B+          | B_POS       |
| 4            | B-          | B_NEG       |
| 5            | AB+         | AB_POS      |
| 6            | AB-         | AB_NEG      |
| 7            | O+          | O_POS       |
| 8            | O-          | O_NEG       |
| 9            | Unknown     | UNKNOWN     |

### RELATIONSHIP
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | Spouse      | SPOUSE      |
| 2            | Son         | SON         |
| 3            | Daughter    | DAUGHTER    |
| 4            | Father      | FATHER      |
| 5            | Mother      | MOTHER      |
| 6            | Sibling     | SIBLING     |
| 7            | Friend      | FRIEND      |
| 8            | Guardian    | GUARDIAN    |
| 9            | Other       | OTHER       |

### PATIENT_STATUS
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | Admitted    | ADMITTED    |
| 2            | Discharged  | DISCHARGED  |
| 3            | On Leave    | ON_LEAVE    |
| 4            | Critical    | CRITICAL    |
| 5            | Stable      | STABLE      |

### NOTES_TYPE
| lookup_value | lookup_item       | lookup_code       |
|--------------|-------------------|-------------------|
| 1            | General           | GENERAL           |
| 2            | Nursing           | NURSING           |
| 3            | Physician         | PHYSICIAN         |
| 4            | Incident          | INCIDENT          |
| 5            | Discharge Summary | DISCHARGE_SUMMARY |

### SURGERY_TYPE
| lookup_value | lookup_item         | lookup_code         |
|--------------|---------------------|---------------------|
| 1            | Elective Surgery    | ELECTIVE_SURGERY    |
| 2            | Emergency Surgery   | EMERGENCY_SURGERY   |
| 3            | Diagnostic Procedure| DIAGNOSTIC_PROCEDURE|
| 4            | Therapeutic         | THERAPEUTIC         |
| 5            | Other               | OTHER               |

### DIAGNOSIS_STATUS
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | Active      | ACTIVE      |
| 2            | Resolved    | RESOLVED    |
| 3            | Monitoring  | MONITORING  |
| 4            | Ruled Out   | RULED_OUT   |

### CLINICAL_NOTE_PRIORITY
| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 1            | High        | HIGH        |
| 2            | Medium      | MEDIUM      |
| 3            | Low         | LOW         |

### MEDICATION_SLOT_TIME

The `lookup_value` here is the **actual hour (24h)** used to compute `scheduled_time` in `medication_slot`.

| lookup_value | lookup_item | lookup_code |
|--------------|-------------|-------------|
| 8            | Morning     | MORNING     |
| 13           | Afternoon   | AFTERNOON   |
| 18           | Evening     | EVENING     |
| 22           | Night       | NIGHT       |

Admin can change `lookup_value` (e.g. change Morning from 8 to 7) via `PUT /api/masters/{id}` — no code change needed. The slot generation service reads this table to resolve the actual hour.

---

## How It Is Used in the Application

**Backend (entity field):**
```
medical.blood_type = "A_POS"   ← stores lookup_code
```

**API response (enriched):**
```json
{
  "bloodType": "A_POS",
  "bloodTypeDisplay": "A+"
}
```
The service joins master_table to resolve the display label when building response DTOs.

**Frontend flow:**
1. On page load → `GET /api/masters/GENDER` → populate Gender dropdown
2. User selects "Female" → frontend sends `lookup_code = "FEMALE"` in request body
3. Backend stores `"FEMALE"` in the entity field

---

## APIs

| Method | Path                  | Role     | Description                    |
|--------|-----------------------|----------|--------------------------------|
| GET    | /api/masters/{type}   | Any auth | Get all values for a type      |
| POST   | /api/masters          | ADMIN    | Add a new lookup value         |
| PUT    | /api/masters/{id}     | ADMIN    | Update a lookup value          |

---

## Open Question for MasterTable

**OQ-10 (confirmed):** `lookup_code` is unique within `type` only, not globally.
So `GENDER.OTHER` and `RELATIONSHIP.OTHER` can both exist — they are different rows with different `type`.
