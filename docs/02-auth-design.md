# Authentication & Authorization Design

## Flow

```
POST /api/auth/login
  → AuthController
  → AuthenticationManager.authenticate(username, password)
  → AppUserDetailsService.loadUserByUsername()
      → loads User + roles from DB
  → JwtUtil.generateToken(username, roles)
  → returns { token, expiresIn, roles }

Every subsequent request:
  → JwtFilter (OncePerRequestFilter)
      → extract Bearer token
      → validate token (signature + expiry)
      → load UserDetails
      → set SecurityContext with authorities (ROLE_NURSE etc.)
  → Spring Security evaluates @PreAuthorize on controller method
```

---

## Tables Involved

- `user` — credentials (username, bcrypt password, user_type)
- `user_details` — profile info (name, email, phone, designation)
- `role` — role catalogue (ROLE_NURSE, ROLE_ADMIN, etc.)
- `role_access` — maps user → role (many-to-many bridge)

---

## Role Hierarchy (SecurityConfig)

```
ROLE_SUPER_ADMIN > ROLE_ADMIN
ROLE_ADMIN > ROLE_NURSE
ROLE_ADMIN > ROLE_DOCTOR
ROLE_ADMIN > ROLE_RECEPTIONIST
```

Adding a new elevated role in future = one line change in hierarchy, zero endpoint annotation changes.

---

## JWT Details

| Property        | Value                        |
|-----------------|------------------------------|
| Library         | jjwt 0.12.x                  |
| Algorithm       | HS256                        |
| Secret          | Externalized in `application.properties` (`jwt.secret`) |
| Expiry          | 10 hours (configurable)      |
| Claims          | `sub` = username, `roles` = list |

Token blacklist (logout) stored in `invalidated_token` table, cleaned up by scheduled job.

---

## Endpoints

| Method | Path                  | Access      | Description          |
|--------|-----------------------|-------------|----------------------|
| POST   | /api/auth/login       | Public      | Login, returns JWT   |
| POST   | /api/auth/logout      | Authenticated | Blacklist token    |
| GET    | /api/health           | Public      | Health check         |
| GET    | /actuator/**          | Public (dev) / ADMIN (prod) | Actuator |

---

## Password Policy

- Stored using `BCryptPasswordEncoder` (strength 10)
- Plain text never stored or logged

---

## Global Exception Handler

`@RestControllerAdvice` on `GlobalExceptionHandler` catches:

| Exception                        | HTTP Status |
|----------------------------------|-------------|
| `ElderCareException`             | 400         |
| `ResourceNotFoundException`      | 404         |
| `AccessDeniedException`          | 403         |
| `AuthenticationException`        | 401         |
| `MethodArgumentNotValidException`| 422         |
| `Exception` (fallback)           | 500         |

All error responses follow a standard shape:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Patient not found",
  "timestamp": "2025-01-01T10:00:00"
}
```
