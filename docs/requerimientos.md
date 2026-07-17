# Requirements

This page lists the functional requirements fulfilled by the CC Identity Service.

## Functional requirements

| ID | Requirement | Endpoints |
|----|-------------|-----------|
| FR-AUTH-1 | Institutional email + password login | `POST /api/v1/auth/login` |
| FR-AUTH-2 | Google OAuth 2.0 login | `POST /api/v1/auth/login/google` |
| FR-AUTH-3 | OTP verification (two-factor) | `POST /api/v1/otp/validate` |
| FR-AUTH-4 | OTP resend with cooldown | `POST /api/v1/otp/resend` |
| FR-AUTH-5 | JWT issuance after OTP | `POST /api/v1/otp/validate` |
| FR-AUTH-6 | JWT validation for other services | `POST /api/v1/token/validate` |
| FR-AUTH-7 | Logout and token revocation | `POST /api/v1/auth/logout` |
| FR-AUTH-8 | Password recovery request | `POST /api/v1/password/recovery` |
| FR-AUTH-9 | Password reset with recovery code | `POST /api/v1/password/reset` |
| FR-AUTH-10 | Account lockout after N failed attempts | enforced in `User.registerFailedLoginAttempt` |
| FR-AUDIT-1 | Security audit query (ADMIN only) | `GET /api/v1/audit` |
| FR-INT-1 | Internal credential creation | `POST /api/v1/internal/credentials` |
| FR-INT-2 | Internal role update | `PUT /api/v1/internal/credentials/{userId}/role` |
| FR-INT-3 | Internal status update | `PUT /api/v1/internal/credentials/{userId}/status` |
| FR-INT-4 | Internal email resolution | `GET /api/v1/internal/credentials/{userId}/email` |

## Non-functional requirements

- **Security**: passwords stored with BCrypt; JWT signed with HS256; OTP and recovery codes are single-use and time-bound.
- **Resilience**: account auto-unlocks after the configured lockout window; OTP resend cooldown prevents abuse.
- **Auditability**: every security-relevant action emits an `AuditEvent` (login, logout, OTP validated, password reset, role/status changes).
- **Interoperability**: JWT is the single cross-service trust mechanism; internal endpoints are network-protected and one requires an API key.

[Back to top](#requirements)
