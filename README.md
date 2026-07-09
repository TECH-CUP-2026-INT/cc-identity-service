
**SCRUM tasks:** SCRUM-13 (OTP verification), SCRUM-14 (user authentication), SCRUM-15 (session management and JWT validation)

---

## Tech Stack

Java 21 · Spring Boot 3.5.6 · Maven · PostgreSQL · JWT (jjwt 0.12.5) · Spring Security · Spring Mail · MapStruct · Jacoco · Swagger (springdoc 2.6.0) · Docker · GitHub Actions

---

## Project Structure

```
src/
├── main/
│   ├── java/co/edu/escuelaing/techcup/identity/
│   │   ├── entity/          # userEntity, otpCodeEntity
│   │   ├── repository/      # userRepository, otpCodeRepository
│   │   ├── dto/             # Request and response objects
│   │   ├── service/         # Business logic
│   │   ├── controller/      # REST endpoints
│   │   ├── config/          # Security, JWT, Swagger, CORS
│   │   ├── mapper/          # MapStruct mappers
│   │   └── exception/       # Global exception handling
│   └── resources/
│       ├── application.yml       # Base config
│       ├── application-dev.yml   # Local dev (PostgreSQL + Mailtrap)
│       └── application-prod.yml  # Production (env vars only)
└── test/
    └── resources/
        └── application.yml   # H2 in-memory for tests
```

---

## Configuration

Profiles: `dev` (default) and `prod`. Switch with `SPRING_PROFILES_ACTIVE=prod`.

Required environment variables in prod:

| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USER` | Database username |
| `DB_PASS` | Database password |
| `JWT_SECRET` | Secret key for signing JWT tokens (min 256 bits) |
| `MAIL_HOST` | SMTP host |
| `MAIL_PORT` | SMTP port |
| `MAIL_USER` | SMTP username |
| `MAIL_PASS` | SMTP password |

---

## Running locally

```bash
mvn spring-boot:run
```

API docs available at: `http://localhost:11711/swagger-ui/index.html#/`

---

## Running tests

```bash

docker run --name identity-db -e POSTGRES_DB=identity_db -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:15

mvn verify
```

Coverage report is at: `target/site/jacoco/index.html`
