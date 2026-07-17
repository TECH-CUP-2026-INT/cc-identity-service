# API

All endpoints are prefixed with `/api/v1`. Interactive documentation is available via Swagger UI at `/swagger-ui.html`.

## Authentication — `AuthController`

| Method | Path | Summary | Success | Common errors |
|--------|------|---------|---------|---------------|
| `POST` | `/api/v1/auth/login` | Institutional email + password login (sends OTP) | `200` OTP sent | `401` invalid credentials, `403` inactive/locked |
| `POST` | `/api/v1/auth/login/google` | Google OAuth 2.0 login (sends OTP) | `200` OTP sent | `400` invalid token, `403` blocked/inactive |
| `POST` | `/api/v1/otp/validate` | Verify OTP and receive JWT + user | `200` token issued | `400` incorrect/expired OTP |
| `POST` | `/api/v1/otp/resend` | Resend OTP (cooldown applies) | `200` resent | `400` cooldown, `404` user not found |
| `POST` | `/api/v1/password/recovery` | Request password recovery code | `200` (always, for security) | `400` invalid email |
| `POST` | `/api/v1/password/reset` | Reset password with recovery code | `200` reset | `400` invalid, `410` expired |
| `POST` | `/api/v1/token/validate` | Validate JWT (`Bearer` header) | `200` user data | `400`/`401` invalid/expired/revoked |
| `POST` | `/api/v1/auth/logout` | Revoke JWT on logout | `200` closed | `401` bad token |

### Example — login then validate OTP

```bash
# Step 1: login (returns userId, emails an OTP)
curl -X POST http://localhost:5620/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"student@escuelaing.edu.co","password":"s3cr3t"}'

# Step 2: validate OTP -> JWT
curl -X POST http://localhost:5620/api/v1/otp/validate \
  -H "Content-Type: application/json" \
  -d '{"userId":"<userId>","otpCode":"123456"}'
```

## Audit — `AuditController` (ADMIN only)

| Method | Path | Summary | Success | Errors |
|--------|------|---------|---------|--------|
| `GET` | `/api/v1/audit` | Query audit events (filters: `startDate`, `endDate`, `actionType`, `userId`) | `200` list | `400` bad params, `401` no JWT, `403` not ADMIN |

## Internal — `InternalCredentialController` (`@Hidden`)

These endpoints are for inter-service use only and must not be exposed to end users.

| Method | Path | Summary | Success |
|--------|------|---------|---------|
| `POST` | `/api/v1/internal/credentials` | Create credentials for a `users-players-service` user | `201` |
| `PUT` | `/api/v1/internal/credentials/{userId}/role` | Update user role (captain promotion / captaincy transfer) | `200` |
| `PUT` | `/api/v1/internal/credentials/{userId}/status` | Update account status (admin disable) | `200` |
| `GET` | `/api/v1/internal/credentials/{userId}/email` | Resolve email by userId (requires `X-Internal-Api-Key`) | `200` |

[Back to top](#api)
