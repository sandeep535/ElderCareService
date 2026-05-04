# API Inventory

Base path: `/api`

All protected endpoints require `Authorization: Bearer <token>` header.

This inventory shows what is currently implemented in code and what is still planned.

---

## Implemented APIs

### Auth

| Method | Path               | Role    | Description          |
|--------|--------------------|---------|----------------------|
| POST   | /auth/signup       | Public  | Create user + roles  |
| POST   | /auth/login        | Public  | Login → JWT token    |
| POST   | /auth/logout       | Any auth| Invalidate token     |

---

### Health

| Method | Path      | Role   | Description              |
|--------|-----------|--------|--------------------------|
| GET    | /health   | Public | Simple up/alive check    |

---

### Actuator

| Method | Path              | Role   | Description              |
|--------|-------------------|--------|--------------------------|
| GET    | /actuator/health  | Public | Spring Boot health       |
| GET    | /actuator/mappings| ADMIN  | All registered endpoints |

---

### Role Management

| Method | Path            | Role  | Description                            |
|--------|-----------------|-------|----------------------------------------|
| GET    | /roles          | ADMIN | List all roles                         |
| POST   | /roles          | ADMIN | Create role (ROLE_ auto-prepended)     |

---

### Patient

| Method | Path                  | Role            | Description              |
|--------|-----------------------|-----------------|--------------------------|
| POST   | /patients             | NURSE,DOCTOR    | Register new patient     |
| GET    | /patients             | Any auth        | List patients            |
| GET    | /patients/{id}        | Any auth        | Get patient by id        |
| PUT    | /patients/{id}        | NURSE,DOCTOR    | Update patient           |

---

### Next of Kin (NOK)

| Method | Path                        | Role            | Description     |
|--------|-----------------------------|-----------------|-----------------|
| POST   | /patients/{id}/nok          | NURSE,DOCTOR    | Add NOK         |
| GET    | /patients/{id}/nok          | Any auth        | List NOKs       |
| PUT    | /patients/{id}/nok/{nokId}  | NURSE,DOCTOR    | Update NOK      |

---

### Medical

| Method | Path                    | Role            | Description          |
|--------|-------------------------|-----------------|----------------------|
| POST   | /patients/{id}/medical  | NURSE,DOCTOR    | Save medical info    |
| GET    | /patients/{id}/medical  | Any auth        | Get medical info     |

---

### Admission

| Method | Path                      | Role            | Description        |
|--------|---------------------------|-----------------|--------------------|
| POST   | /patients/{id}/admission  | NURSE,DOCTOR    | Create admission   |
| GET    | /patients/{id}/admission  | Any auth        | Get admission      |

---

### Notes

| Method | Path                    | Role     | Description     |
|--------|-------------------------|----------|-----------------|
| POST   | /patients/{id}/notes    | Any auth | Add note        |
| GET    | /patients/{id}/notes    | Any auth | List notes      |

---

### Vitals

| Method | Path                     | Role            | Description         |
|--------|--------------------------|-----------------|---------------------|
| POST   | /patients/{id}/vitals    | NURSE,DOCTOR    | Record vitals       |
| GET    | /patients/{id}/vitals    | Any auth        | List vitals history |

---

### Address

| Method | Path                          | Role            | Description              |
|--------|-------------------------------|-----------------|--------------------------|
| POST   | /addresses                    | NURSE,DOCTOR    | Save address (type+typeId)|
| GET    | /addresses/{type}/{typeId}    | Any auth        | Get address by owner      |

---

### Patient Journey

| Method | Path                          | Role     | Description              |
|--------|-------------------------------|----------|--------------------------|
| GET    | /patients/{id}/journey        | Any auth | Get completion status    |

---

## Planned APIs

These routes are designed in docs but are not yet implemented in code.

### User Management

| Method | Path              | Role  | Description              |
|--------|-------------------|-------|--------------------------|
| POST   | /users            | ADMIN | Create user + details    |
| GET    | /users            | ADMIN | List all users           |
| GET    | /users/{id}       | ADMIN | Get user by id           |
| PUT    | /users/{id}       | ADMIN | Update user              |
| DELETE | /users/{id}       | ADMIN | Deactivate user          |

---

### Audit Log

| Method | Path                                  | Role  | Description                     |
|--------|---------------------------------------|-------|---------------------------------|
| GET    | /patients/{id}/audit                  | ADMIN | Activity timeline for patient   |
| GET    | /patients/{id}/audit?type=VITALS      | ADMIN | Filter timeline by module       |
| GET    | /audit/failures                       | ADMIN | View all failed audit writes    |
| PUT    | /audit/failures/{id}/resolve          | ADMIN | Mark failure as resolved        |

---

### Medication Master

| Method | Path                          | Role     | Description                  |
|--------|-------------------------------|----------|------------------------------|
| GET    | /medication-master            | Any auth | Search medicine catalogue    |
| POST   | /medication-master            | ADMIN    | Add medicine to catalogue    |
| PUT    | /medication-master/{id}       | ADMIN    | Update medicine              |

---

### Patient Medication & Slots

| Method | Path                                                | Role        | Description                |
|--------|-----------------------------------------------------|-------------|----------------------------|
| POST   | /patients/{id}/medications                          | ADMIN,NURSE | Prescribe + generate slots |
| GET    | /patients/{id}/medications                          | Any auth    | List active prescriptions  |
| PUT    | /patients/{id}/medications/{pmId}/stop              | ADMIN,NURSE | Stop a prescription        |
| GET    | /patients/{id}/medications/next-due                 | Any auth    | Next due slots             |
| GET    | /patients/{id}/medications/slots                    | Any auth    | All slots (filter by date) |
| PUT    | /patients/{id}/medications/slots/{slotId}/given     | NURSE,ADMIN | Mark slot as given         |
| PUT    | /patients/{id}/medications/slots/{slotId}/skipped   | NURSE,ADMIN | Mark slot as skipped       |

---

### Clinical Notes

| Method | Path                              | Role     | Description          |
|--------|-----------------------------------|----------|----------------------|
| POST   | /patients/{id}/clinical-notes     | Any auth | Add clinical note    |
| GET    | /patients/{id}/clinical-notes     | Any auth | List clinical notes  |
| PUT    | /patients/{id}/clinical-notes/{cId} | Any auth | Update            |

---

### Medical History

| Method | Path                               | Role        | Description          |
|--------|------------------------------------|-------------|----------------------|
| POST   | /patients/{id}/medical-history     | ADMIN,NURSE | Add history entry    |
| GET    | /patients/{id}/medical-history     | Any auth    | List history         |
| PUT    | /patients/{id}/medical-history/{hId} | ADMIN,NURSE | Update           |

---

### Diagnoses

| Method | Path                          | Role        | Description       |
|--------|-------------------------------|-------------|-------------------|
| POST   | /patients/{id}/diagnoses      | ADMIN,NURSE | Add diagnosis     |
| GET    | /patients/{id}/diagnoses      | Any auth    | List diagnoses    |
| PUT    | /patients/{id}/diagnoses/{dId}| ADMIN,NURSE | Update diagnosis  |

---

### Masters

| Method | Path                          | Role  | Description              |
|--------|-------------------------------|-------|--------------------------|
| GET    | /masters/{type}               | Any auth | Get lookup list by type|
| POST   | /masters                      | ADMIN | Add master entry         |
| PUT    | /masters/{id}                 | ADMIN | Update master entry      |

---

### Diagnosis Master

| Method | Path                          | Role     | Description              |
|--------|-------------------------------|----------|--------------------------|
| GET    | /diagnosis-master             | Any auth | Search/list ICD codes    |
| POST   | /diagnosis-master             | ADMIN    | Add ICD entry            |
| PUT    | /diagnosis-master/{id}        | ADMIN    | Update ICD entry         |
