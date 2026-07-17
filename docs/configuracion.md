# Configuration

All configuration lives in `src/main/resources/application.yml` and is overridable through environment variables (12-factor friendly). The service listens on port **5620** by default.

## Core settings

| Property | Env var | Default | Description |
|----------|---------|---------|-------------|
| `server.port` | `SERVER_PORT` | `5620` | HTTP port |
| `spring.application.name` | – | `service-identity` | Service name |

## MongoDB

| Property | Env var | Default |
|----------|---------|---------|
| `spring.data.mongodb.uri` | `MONGODB_URI` | `mongodb://localhost:27017/identity_service` |

Collections: users, OTP tokens, recovery tokens, revoked tokens (TTL index), session activity, audit events, user profile snapshots.

## JWT

| Property | Env var | Default |
|----------|---------|---------|
| `jwt.secret` | `JWT_SECRET` | `defaultSecretKeyForDevelopmentOnlyChangeInProduction2026!` |
| `jwt.expiration-ms` | `JWT_EXPIRATION` | `3600000` (1 h) |

!!! warning "Change in production"
    The default JWT secret is for development only. Always set `JWT_SECRET` to a strong, unique value in production.

## Auth / OTP / Recovery tunables

| Group | Property | Env var | Default |
|-------|----------|---------|---------|
| `auth` | `max-failed-login-attempts` | `AUTH_MAX_FAILED_LOGIN_ATTEMPTS` | `5` |
| `auth` | `lockout-duration-minutes` | `AUTH_LOCKOUT_DURATION_MINUTES` | `15` |
| `auth` | `inactivity-timeout-minutes` | `AUTH_INACTIVITY_TIMEOUT_MINUTES` | `30` |
| `otp` | `expiration-minutes` | `OTP_EXPIRATION_MINUTES` | `5` |
| `otp` | `max-attempts` | `OTP_MAX_ATTEMPTS` | `3` |
| `otp` | `resend-cooldown-seconds` | `OTP_RESEND_COOLDOWN` | `60` |
| `otp` | `length` | `OTP_LENGTH` | `6` |
| `recovery` | `expiration-minutes` | `RECOVERY_EXPIRATION_MINUTES` | `15` |

## Integrations

| Property | Env var | Default | Purpose |
|----------|---------|---------|---------|
| `spring.security.oauth2.client.registration.google.*` | `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | placeholder | Google OAuth 2.0 |
| `spring.mail.*` | `MAIL_HOST` / `MAIL_PORT` / `MAIL_USERNAME` / `MAIL_PASSWORD` | `smtp.gmail.com:587` | OTP / recovery email |
| `techcup.security.internal.api-key` | `INTERNAL_API_KEY` | empty | Service-to-service key (email resolution) |
| `users.service.base-url` | `USERS_SERVICE_URL` | `http://localhost:8084/api/v1` | `users-players-service` (Feign) |

## Swagger / OpenAPI

| Property | Value |
|----------|-------|
| `springdoc.api-docs.path` | `/v3/api-docs` |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` |

[Back to top](#configuration)
