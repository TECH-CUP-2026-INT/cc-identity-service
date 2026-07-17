# Appendices

## Glossary

| Term | Definition |
|------|------------|
| JWT | JSON Web Token (HS256) issued by Identity after OTP verification; the cross-service trust mechanism. |
| OTP | One-Time Password — 6-digit code emailed during login; second authentication factor. |
| Recovery code | Single-use, expiring code emailed for password reset. |
| Revoked token | JWT added to a TTL-backed list on logout; rejected by the security filter. |
| UserType | `STUDENT`, `GUEST` or `GRADUATE`. |
| UserRole | `PLAYER`, `CAPTAIN`, `REFEREE`, `ORGANIZER` or `ADMIN`. |
| AccountStatus | `ACTIVE`, `INACTIVE` or `LOCKED`. |
| Hexagonal architecture | Ports & adapters style; domain is framework-agnostic. |

## Environment variables (quick reference)

`SERVER_PORT`, `MONGODB_URI`, `JWT_SECRET`, `JWT_EXPIRATION`, `AUTH_MAX_FAILED_LOGIN_ATTEMPTS`, `AUTH_LOCKOUT_DURATION_MINUTES`, `AUTH_INACTIVITY_TIMEOUT_MINUTES`, `OTP_EXPIRATION_MINUTES`, `OTP_MAX_ATTEMPTS`, `OTP_RESEND_COOLDOWN`, `OTP_LENGTH`, `RECOVERY_EXPIRATION_MINUTES`, `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `GOOGLE_CLIENT_ID`, `GOOGLE_CLIENT_SECRET`, `INTERNAL_API_KEY`, `USERS_SERVICE_URL`.

## Related services

- [cc-users-players-service](https://github.com/TECH-CUP-2026-INT/cc-users-players-service) — source of truth for profiles, role and status.
- [cc-teams-service](https://github.com/TECH-CUP-2026-INT/cc-teams-service) — team management.
- [mk-tournament-service](https://github.com/TECH-CUP-2026-INT/mk-tournament-service) — tournament lifecycle.
- [am-comunication-service](https://github.com/TECH-CUP-2026-INT/am-comunication-service) — notifications (resolves emails via Identity).

## References

- Spring Boot 3.5.6 — https://spring.io/projects/spring-boot
- JJWT 0.12.6 — https://github.com/jwtk/jjwt
- MapStruct 1.6.3 — https://mapstruct.org/
- springdoc-openapi 2.8.4 — https://springdoc.org/
- Material for MkDocs — https://squidfunk.github.io/mkdocs-material/

[Back to top](#appendices)
