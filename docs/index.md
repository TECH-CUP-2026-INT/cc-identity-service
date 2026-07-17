# CC Identity Service

Microservice responsible for authentication, authorization, credential lifecycle and the security audit log of the TechCup platform.

Spring Boot 3.5.6 + MongoDB

Built with Java 21, hexagonal architecture and MongoDB persistence. It is the source of truth for authentication credentials and issues the JWT tokens consumed by every other TechCup microservice (tournament, teams, users-players, communication) through the API Gateway.

Two-factor authentication (OTP)

Login requires institutional email + password **and** a 6-digit OTP sent by email before a JWT is issued. Google OAuth 2.0 login is supported for guests, referees, alumni and organizers.

Part of the TechCup ecosystem

A microservice within the DOSW platform that digitizes the semester football tournament at Escuela Colombiana de Ingeniería Julio Garavito.

## What does this service do?

`cc-identity-service` centralizes all authentication logic:

- Authenticates users with institutional email (`@escuelaing.edu.co`) + password, plus OTP verification (two-factor).
- Authenticates users via Google OAuth 2.0 (guests, referees, alumni, organizers).
- Issues and validates JWT tokens (HS256) consumed by other microservices.
- Revokes tokens on logout (TTL-backed revocation list in MongoDB).
- Recovers passwords through single-use, expiring recovery codes.
- Manages account lockout after repeated failed login attempts.
- Exposes a security audit log (login, logout, OTP, password reset, role changes) queryable by ADMIN.
- Provides internal endpoints for `users-players-service` (credential creation/role/status) and `am-notification-service` (email resolution).

## Repository

https://github.com/TECH-CUP-2026-INT/cc-identity-service

## Quick start

```bash
# 1. Clone
git clone https://github.com/TECH-CUP-2026-INT/cc-identity-service.git
cd cc-identity-service

# 2. Start MongoDB (or use an existing instance / Docker)
docker run -d --name mongo -p 27017:27017 mongo:7

# 3. Build and run
./mvnw clean package
java -jar target/service-identity-0.0.1-SNAPSHOT.jar
# or, with the Maven wrapper in dev mode:
./mvnw spring-boot:run
```

The service is available at `http://localhost:5620`.

- Swagger UI: `http://localhost:5620/swagger-ui.html`
- OpenAPI docs: `http://localhost:5620/v3/api-docs`

See [Configuration](configuracion.md) and the [REST API](api.md).

[Back to top](#cc-identity-service)
