# CC Identity Service

[![CI - Build, Test, Publish & Deploy](https://github.com/TECH-CUP-2026-INT/cc-identity-service/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/TECH-CUP-2026-INT/cc-identity-service/actions/workflows/ci.yml)
[![Docs](https://github.com/TECH-CUP-2026-INT/cc-identity-service/actions/workflows/deploy-mkdocs.yml/badge.svg?branch=main)](https://github.com/TECH-CUP-2026-INT/cc-identity-service/actions/workflows/deploy-mkdocs.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen)
![MongoDB](https://img.shields.io/badge/MongoDB-7-green)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A580%25-blue)

Identity and authentication microservice for **TechCup**, the platform that digitizes the semester football tournament at Escuela Colombiana de Ingeniería Julio Garavito.

The service owns the authentication credentials and issues the JWTs that every other TechCup microservice (tournament, teams, users-players, communication) accepts through the API Gateway.

📚 **Full documentation:** https://tech-cup-2026-int.github.io/cc-identity-service/

---

## Features

- **Institutional login with two-factor authentication.** Users sign in with an `@escuelaing.edu.co` email and password, then confirm a 6-digit OTP sent by email before a JWT is issued.
- **Google OAuth 2.0 login** for guests, referees, alumni and organizers.
- **JWT issuance and validation** (HS256), with a session inactivity timeout.
- **Token revocation** on logout, backed by a revocation list in MongoDB with a TTL index.
- **Password recovery** with single-use recovery codes that expire.
- **Account lockout** after repeated failed login attempts.
- **Security audit log** of logins, logouts, OTP events, password resets and role changes. Only `ADMIN` users can query it.
- **Internal endpoints** that let `users-players-service` manage credentials and let `am-notification-service` look up a user's email.

## Tech stack

| Area | Technology |
|------|------------|
| Language / runtime | Java 21 |
| Framework | Spring Boot 3.5.6 (Web, Security, Validation, Mail, OAuth2 Client) |
| Persistence | MongoDB (Spring Data MongoDB) |
| Service-to-service calls | Spring Cloud OpenFeign |
| Tokens | JJWT 0.12.6 |
| Mapping / boilerplate | MapStruct 1.6.3, Lombok |
| API docs | springdoc-openapi 2.8.4 (Swagger UI) |
| Testing / coverage | JUnit 5, Mockito, Spring Security Test, JaCoCo (≥ 80% line coverage) |
| Docs site | MkDocs Material |
| CI/CD | GitHub Actions → GHCR → Azure App Service |

## Architecture

The service uses a **hexagonal (ports and adapters)** architecture:

```
src/main/java/co/edu/escuelaing/techcup/identity
├── domain/                  # Core business logic (no framework dependencies)
│   ├── model/               # User, OtpToken, RecoveryToken, RevokedToken, AuditEvent, ...
│   ├── enums/               # UserRole, UserType, AccountStatus, AuditActionType, IdType
│   ├── exception/           # Domain exceptions
│   ├── validator/           # EmailValidator (allowed domains)
│   └── port/
│       ├── in/              # Use case interfaces
│       └── out/             # Repository, email, OAuth and user-profile ports
├── application/usecase/     # Use case implementations
├── infrastructure/
│   ├── adapter/in/rest/     # Controllers, DTOs, global exception handler
│   ├── adapter/out/         # MongoDB, email, Google OAuth, users-players (Feign)
│   ├── config/              # Security (JWT, internal API key, CSRF), CORS, Swagger
│   └── mapper/              # MapStruct mappers
└── shared/util/             # JwtUtil, OtpUtil, PasswordUtil
```

## API overview

All endpoints use the `/api/v1` base path. Swagger UI documents the full request and response schemas.

### Authentication (public)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/auth/login` | Institutional email + password login. Sends an OTP |
| `POST` | `/auth/login/google` | Login with a Google ID token |
| `POST` | `/otp/validate` | Validates the OTP and returns a JWT |
| `POST` | `/otp/resend` | Resends the OTP (with a cooldown) |
| `POST` | `/password/recovery` | Sends a password recovery code |
| `POST` | `/password/reset` | Resets the password using the recovery code |
| `POST` | `/token/validate` | Validates a JWT (used by other services) |
| `POST` | `/auth/logout` | Revokes the current JWT |

### Audit (JWT with `ADMIN` role)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/audit` | Queries audit events (filters by date range and action type) |

### Internal (service-to-service)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/internal/credentials` | Creates credentials for a new user |
| `PUT` | `/internal/credentials/{userId}/role` | Updates a user's role |
| `PUT` | `/internal/credentials/{userId}/status` | Updates a user's account status |
| `GET` | `/internal/credentials/{userId}/email` | Resolves a user's email. **Requires the `X-Internal-Api-Key` header** |
| `POST` | `/internal/credentials/{userId}/revoke-sessions` | Revokes all of a user's active sessions |

**Roles:** `PLAYER`, `CAPTAIN`, `REFEREE`, `ORGANIZER`, `ADMIN`

## Getting started

### Prerequisites

- JDK 21
- Docker, or a reachable MongoDB instance
- SMTP credentials, needed to send OTP and recovery emails
- Google OAuth client credentials, needed only for Google login

### Run locally

```bash
# 1. Clone the repository
git clone https://github.com/TECH-CUP-2026-INT/cc-identity-service.git
cd cc-identity-service

# 2. Start MongoDB
docker compose up -d

# 3. Run the service
./mvnw spring-boot:run
```

The service starts on **http://localhost:5620**:

- Swagger UI: http://localhost:5620/swagger-ui.html
- OpenAPI spec: http://localhost:5620/v3/api-docs

### Run with Docker

```bash
docker build -t cc-identity-service .
docker run -p 5620:5620 \
  -e MONGODB_URI=mongodb://host.docker.internal:27017/identity_service \
  -e JWT_SECRET=change-me \
  cc-identity-service
```

> Note: the Dockerfile declares `EXPOSE 8081`, but the application listens on `SERVER_PORT` (default `5620`). Map the port you configure.

## Configuration

All settings are in `src/main/resources/application.yml`, and each one can be overridden with an environment variable.

| Env var | Default | Description |
|---------|---------|-------------|
| `SERVER_PORT` | `5620` | HTTP port |
| `MONGODB_URI` | `mongodb://localhost:27017/identity_service` | MongoDB connection string |
| `JWT_SECRET` | dev-only value | JWT signing key. **Always set this in production** |
| `JWT_EXPIRATION` | `3600000` | JWT lifetime in ms (1 h) |
| `MAIL_HOST` / `MAIL_PORT` | `smtp.gmail.com` / `587` | SMTP server |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | — | SMTP credentials |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | placeholder | Google OAuth 2.0 client |
| `INTERNAL_API_KEY` | — | Key for internal service-to-service calls |
| `USERS_SERVICE_URL` | `http://localhost:8084/api/v1` | Base URL of `users-players-service` |
| `AUTH_MAX_FAILED_LOGIN_ATTEMPTS` | `5` | Failed attempts allowed before lockout |
| `AUTH_LOCKOUT_DURATION_MINUTES` | `15` | Lockout duration |
| `AUTH_INACTIVITY_TIMEOUT_MINUTES` | `30` | Session inactivity timeout |
| `OTP_EXPIRATION_MINUTES` | `5` | OTP lifetime |
| `OTP_MAX_ATTEMPTS` | `3` | Validation attempts allowed per OTP |
| `OTP_RESEND_COOLDOWN` | `60` | Seconds to wait between OTP resends |
| `OTP_LENGTH` | `6` | Number of OTP digits |
| `RECOVERY_EXPIRATION_MINUTES` | `15` | Recovery code lifetime |

CORS allows `http://localhost:3000`, `http://localhost:4200`, `http://localhost:5173` and `https://techcup.pages.dev`.

## Testing

```bash
# Run the test suite
./mvnw clean test

# Run tests and enforce the JaCoCo coverage gate (≥ 80% line coverage)
./mvnw clean verify
```

The coverage report is written to `target/site/jacoco/index.html`. [TESTING.md](TESTING.md) describes the test strategy.

## CI/CD

GitHub Actions (`.github/workflows/ci.yml`) runs the following jobs:

1. **Build & Test:** `mvn clean verify` against a MongoDB 7 service container. Runs on `feat/**`, `develop`, `main` and on pull requests.
2. **Docker image:** builds the image and pushes it to GitHub Container Registry (`ghcr.io/tech-cup-2026-int/cc-identity-service`).
3. **Deploy:** deploys the `latest` image to Azure App Service. Runs on `main` only.

A second workflow (`deploy-mkdocs.yml`) publishes the documentation site in `docs/` to GitHub Pages.

## Documentation

The project documentation in `docs/` is built with MkDocs Material:

```bash
pip install -r requirements.txt
mkdocs serve
```

## Contributing

1. Create a feature branch from `develop` (for example `feat/your-feature`).
2. Keep tests green and coverage at or above 80%.
3. Open a pull request to `develop`.

---

Maintained by **[TECH-CUP 2026 INT](https://github.com/TECH-CUP-2026-INT)**, Escuela Colombiana de Ingeniería Julio Garavito.
