# Role Hierarchy — Where It Lives & How to Modify

## Where It Is Defined

The role hierarchy lives in `SecurityConfig.java` — a single `@Bean` method.

```java
// SecurityConfig.java
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

This is the **only place** you ever touch when changing who has access to what at a hierarchy level.

---

## How to Read the Hierarchy

```
ROLE_SUPER_ADMIN > ROLE_ADMIN
```
Means: anyone with SUPER_ADMIN automatically has everything ADMIN has.

```
ROLE_ADMIN > ROLE_NURSE
ROLE_ADMIN > ROLE_DOCTOR
```
Means: ADMIN automatically has everything NURSE has AND everything DOCTOR has.

NURSE and DOCTOR are at the **same level** — neither inherits from the other.

---

## How to Modify — Real Scenarios

### Scenario 1: Add a new SUPER_ADMIN role (future)

You already have `ROLE_SUPER_ADMIN` in the hierarchy. Just:
1. Insert a row in the `role` table: `role_name = "Super Admin"`, `role_id = "ROLE_SUPER_ADMIN"`
2. Assign it to the user via `role_access`
3. **Zero code change** — hierarchy already covers it

---

### Scenario 2: Add a new PHARMACIST role that can only manage medications

Step 1 — Add to `role` table via API (ADMIN creates it):
```
role_name = "Pharmacist"
role_id   = "ROLE_PHARMACIST"   ← ROLE_ is auto-appended by the service
```

Step 2 — Add one line to `SecurityConfig.java`:
```java
return RoleHierarchyImpl.fromHierarchy("""
    ROLE_SUPER_ADMIN > ROLE_ADMIN
    ROLE_ADMIN > ROLE_NURSE
    ROLE_ADMIN > ROLE_DOCTOR
    ROLE_ADMIN > ROLE_RECEPTIONIST
    ROLE_ADMIN > ROLE_PHARMACIST        ← add this line
""");
```

Step 3 — Annotate medication endpoints:
```java
@PreAuthorize("hasAnyRole('NURSE', 'PHARMACIST')")
public ResponseEntity<?> prescribeMedication(...) { }
```

ADMIN automatically gets access because `ROLE_ADMIN > ROLE_PHARMACIST`.

---

### Scenario 3: Give DOCTOR same access as NURSE (promote Doctor)

Change:
```
ROLE_ADMIN > ROLE_NURSE
ROLE_ADMIN > ROLE_DOCTOR
```
To:
```
ROLE_ADMIN > ROLE_NURSE
ROLE_NURSE > ROLE_DOCTOR    ← Doctor now inherits everything Nurse has
```

Now any endpoint annotated `@PreAuthorize("hasRole('NURSE')")` also allows DOCTOR.

---

### Scenario 4: Remove a role from hierarchy (demote)

Simply remove the line from the hierarchy string. The role still exists in the DB and can still be assigned — it just no longer inherits from a parent role.

---

## Role API (ADMIN only)

The `role` table is managed via API. The service **auto-prepends `ROLE_`** to whatever `role_id` the admin provides.

| Method | Path          | Role  | Description                        |
|--------|---------------|-------|------------------------------------|
| GET    | /api/roles    | ADMIN | List all roles                     |
| POST   | /api/roles    | ADMIN | Create new role                    |
| PUT    | /api/roles/{id} | ADMIN | Update role name                 |
| DELETE | /api/roles/{id} | ADMIN | Deactivate role                  |

**POST /api/roles request:**
```json
{
  "roleName": "Pharmacist",
  "roleId": "PHARMACIST"
}
```

**Service auto-converts:**
```java
role.setRoleId("ROLE_" + request.getRoleId().toUpperCase());
// stored as: ROLE_PHARMACIST
```

---

## Important Rule

The `role` table and the `SecurityConfig` hierarchy are **two separate things**:

| What | Where | Controls |
|------|-------|----------|
| Role exists | `role` table in DB | Whether a role can be assigned to users |
| Role hierarchy | `SecurityConfig.java` | Which roles inherit from which |

A role can exist in DB but not be in the hierarchy — it will still work, it just won't inherit from any parent. That is fine for leaf-level roles like NURSE, DOCTOR.

---

## Summary — When Do You Touch SecurityConfig?

Only when you want to **change inheritance** — e.g. a new role should have all permissions of an existing role automatically.

For simply **adding a new role** that has its own specific permissions → just add to DB via API + annotate the relevant endpoints. No SecurityConfig change needed.
