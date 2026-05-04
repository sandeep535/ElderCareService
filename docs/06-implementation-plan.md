# Implementation Plan

## Phase 1 — Project Skeleton

- [x] Generate Spring Boot 3.3.x project (Maven, Java 17, JAR)
- [x] Add dependencies: web, security, jpa, mysql, lombok, jjwt 0.12.x, log4j2, actuator
- [x] Configure `application.properties` (DB, JWT secret, port)
- [x] `BaseEntity` with audit columns (`@MappedSuperclass`)
- [x] `ElderCareException` + `ResourceNotFoundException`
- [x] `GlobalExceptionHandler` (`@RestControllerAdvice`)
- [x] `HealthController` → `GET /api/health`
- [x] Actuator config

## Phase 2 — Auth

- [x] `user`, `role`, `role_access`, `invalidated_token` entities + repositories
- [x] `AppUserDetailsService` (loads user + roles from DB)
- [x] `JwtUtil` (generate, validate, extract — jjwt 0.12.x)
- [x] `JwtFilter` (OncePerRequestFilter)
- [x] `SecurityConfig` (filter chain + RoleHierarchy)
- [x] `AuthController` → login, logout, signup
- [ ] `UserController` → CRUD (ADMIN only)

## Phase 3 — Patient Core

- [x] `patient` entity + repository + service + controller
- [x] `patient_journey` auto-created on patient save
- [x] `nok` entity + repository + service + controller
- [x] `address` entity + repository + service + controller

## Phase 4 — Clinical Data

- [x] `medical` (one per patient)
- [x] `admission`
- [x] `notes`
- [x] `vital` (time-series)
- [ ] `medication`
- [ ] `clinical_note`
- [ ] `medical_history`
- [ ] `diagnoses`

## Phase 5 — Masters & Audit

- [ ] `master_table` entity + controller
- [ ] `diagnosis_master` entity + controller
- [ ] `audit_log` — write on every clinical save, read-only API

## Phase 6 — Polish

- [x] Log4j2 configuration (`log4j2.xml`)
- [ ] Pagination on list endpoints
- [x] Input validation (`@Valid` + `@NotBlank` etc.)
- [x] Token cleanup scheduler

---

## Implementation Order for APIs (session by session)

1. Health check + actuator
2. Login / logout
3. User CRUD
4. Patient register + list + get
5. NOK
6. Medical
7. Admission
8. Notes
9. Vitals
10. Medication
11. Clinical Notes
12. Medical History
13. Diagnoses
14. Address
15. Masters
16. Diagnosis Master
17. Audit log
18. Patient Journey (auto, but expose GET)

---

## Current Status Summary

- Phase 1 complete.
- Phase 2 complete except for admin-only user CRUD.
- Phase 3 complete.
- Phase 4 core forms are implemented through medical, admission, notes, and vitals. Medication tracking, clinical notes, medical history, and diagnosis modules remain pending.
- Phase 5 is pending: master tables, diagnosis master API, and audit log read endpoint.
- Phase 6 polish work still needs pagination and API-level read audit access.

## Next Implementation Steps

1. Boot the app and verify `/eldercare/api/health`.
2. Seed the DB and run auth smoke tests for signup/login/logout.
3. Confirm patient flow: create patient, fetch patient, fetch journey, create NOK, create address.
4. Complete medication tracking module with slot generation and missed-slot scheduler.
5. Add master table and diagnosis master endpoints.
6. Expose the audit log read API and finish the remaining clinical modules.
