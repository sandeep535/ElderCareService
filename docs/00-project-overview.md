# ElderCare Service — Project Overview

## Tech Stack

| Layer        | Technology                                      |
|--------------|-------------------------------------------------|
| Language     | Java 17                                         |
| Framework    | Spring Boot 3.3.x (latest stable)               |
| Packaging    | JAR                                             |
| Security     | Spring Security + JWT (jjwt 0.12.x)             |
| ORM          | Spring Data JPA + Hibernate                     |
| DB           | MySQL 8 (ElderCareDB)                           |
| Dialect      | `org.hibernate.dialect.MySQLDialect`            |
| Logging      | Log4j2                                          |
| Boilerplate  | Lombok + Java 17 Records (for DTOs/responses)   |
| Actuator     | Spring Boot Actuator (health + endpoint listing)|
| Build        | Maven (jar packaging)                           |

---

## Project Structure (planned)

```
com.eldercare.service
├── config/
│   └── SecurityConfig.java
├── controller/
├── dto/           ← Java 17 Records for request/response
├── model/         ← JPA Entities with Lombok
├── repository/
├── service/
├── filters/
│   └── JwtFilter.java
├── utils/
│   └── JwtUtil.java
├── exception/
│   ├── ElderCareException.java
│   └── GlobalExceptionHandler.java
└── ElderCareServiceApplication.java
```

---

## Common Audit Columns

All tables (except `audit_log`) include:

| Column      | Type      | Notes                        |
|-------------|-----------|------------------------------|
| captured_by | VARCHAR   | User who created the record  |
| created_on  | DATETIME  | Auto-set on insert           |
| edited_on   | DATETIME  | Auto-set on update           |
| edited_by   | VARCHAR   | User who last modified       |

Implemented via a `BaseEntity` JPA `@MappedSuperclass`.

---

## Open Questions

See `01-open-questions.md`
