# Open Questions

## Status Summary

| OQ   | Topic                        | Status    |
|------|------------------------------|-----------|
| OQ-1 | Admin allow-all strategy     | ✅ Resolved — RoleHierarchy |
| OQ-2 | JWT library version          | ✅ Resolved — jjwt 0.12.x  |
| OQ-3 | staffType vs role            | ✅ Resolved — see below     |
| OQ-4 | patient_journey purpose      | ✅ Resolved — completion tracker |
| OQ-5 | Address polymorphic          | ✅ Resolved — single table  |
| OQ-6 | current_medication vs medication table | ✅ Resolved — both maintained |
| OQ-7 | ClinicalNote recorded_by     | ✅ Resolved — FK to user.id |
| OQ-8 | Vitals time-series + alert   | ✅ Resolved — see 09-vitals-alert-design.md |
| OQ-9 | DiagnosisMaster vs Diagnoses | ✅ Resolved — master-detail |
| OQ-10| MasterTable lookup_code scope| ✅ Resolved — unique within type |
| OQ-11| Vital thresholds in DB or properties | 🔵 Deferred — start with properties |

## OQ-1 — Admin "Allow All" Strategy

**Problem:** ADMIN should have access to every API. In future a SUPER_ADMIN role may be added and should also get full access without code changes.

**Options:**

| Option | Approach | Pros | Cons |
|--------|----------|------|------|
| A | Annotate every endpoint with `@PreAuthorize("hasAnyRole('ADMIN','NURSE',...)")` | Explicit | Must update every annotation when new role added |
| B | In `SecurityConfig`, add a blanket rule: `hasRole('ADMIN')` bypasses all role checks at the filter level | Centralised | Still needs code change for SUPER_ADMIN |
| C ✅ | Define a **privilege hierarchy** in `SecurityConfig` using `RoleHierarchy`. Map `ADMIN > all roles`, `SUPER_ADMIN > ADMIN`. Endpoint annotations stay unchanged. | Zero code change for new elevated roles | Hierarchy must be maintained in config |
| D | Store role-to-permission mappings in DB (`role_access` table) and evaluate dynamically in a custom `AccessDecisionVoter` | Fully DB-driven, zero code change | More complex implementation |

**Recommendation:** Start with **Option C** (RoleHierarchy). It satisfies the "no code change for SUPER_ADMIN" requirement with minimal complexity. If full DB-driven access control is needed later, migrate to Option D.

**Implementation sketch (Option C):**
```java
@Bean
RoleHierarchy roleHierarchy() {
    return RoleHierarchyImpl.fromHierarchy("""
        ROLE_SUPER_ADMIN > ROLE_ADMIN
        ROLE_ADMIN > ROLE_NURSE
        ROLE_ADMIN > ROLE_DOCTOR
        ROLE_ADMIN > ROLE_RECEPTIONIST
    """);
}
```
Endpoint annotations only need the specific role:
```java
@PreAuthorize("hasRole('NURSE')")   // ADMIN and SUPER_ADMIN automatically pass
```

---

## OQ-2 — JWT Library Version

EMR Service uses `jjwt 0.9.1` (old API). For ElderCare we will use `jjwt 0.12.x` which has a cleaner, non-deprecated API.

---

## OQ-3 — staffType vs Role

The `user` table has a `staff_type` column (e.g. Nurse) and there is a separate `role` table with `role_id` (e.g. NURSE). 

**Decision needed:** Is `staff_type` a display label and `role_id` the security role, or are they the same concept?

**Proposed:** `staff_type` = human-readable designation stored on the user. `role_id` (e.g. `ROLE_NURSE`) = Spring Security authority loaded at login from `role_access` join. They can differ (a staff member could have an elevated role).

---

## OQ-4 — PatientJourney (`patient_journey`) purpose

The `patient_journey` table has flags `is_medical`, `is_admission`, `is_note`. 

**Question:** Is this a checklist/progress tracker showing which sections of a patient's record have been filled? Or is it a workflow state machine?

**Proposed:** Treat it as a **completion tracker** — each flag is set to `true` when the corresponding section (Medical, Admission, Notes) is first saved for that patient.

---

## OQ-5 — Address `type` / `typeId` polymorphic link

The `address` table has `type` (e.g. PATIENT, STAFF) and `type_id` (the FK value). 

**Question:** Should this be a polymorphic association or separate address tables per entity?

**Proposed:** Keep single `address` table with `type` enum + `type_id`. No JPA polymorphic mapping needed — queries will always filter by `type`.

---

## OQ-6 — Medication vs MedicalHistory medication field

`medical` table has `current_medication` (free text?) and there is a separate `medication` table with `name`, `dose`, `sig`.

**Question:** Is `current_medication` in `medical` a summary field, and `medication` table holds the structured prescription list?

**Proposed:** Yes — `current_medication` in `medical` is a legacy/summary text. `medication` table is the structured list linked to patient.

---

## OQ-7 — ClinicalNote `recorded_by` — FK to user or free text?

**Proposed:** FK to `user.id`.

---

## OQ-8 — Vital signs — one record per visit or cumulative?

**Question:** Is each `vital` row a snapshot at a point in time, or is there one row per patient updated in place?

**Proposed:** One row per reading (append-only), with `created_on` as the timestamp. This allows trending.

---

## OQ-9 — DiagnosisMaster vs Diagnoses

`diagnosis_master` holds the ICD code catalogue. `diagnoses` links a patient to a master entry with date/status/notes.

**Confirmed:** Standard master-detail pattern.

---

## OQ-10 — MasterTable scope

`master_table` covers: Gender, BloodType, RelationshipType, PatientStatus, NotesType, SurgeryType.

**Question:** Should `lookup_code` be unique across all types or only within a type?

**Proposed:** Unique within `type` only. Queries always filter by `type`.
