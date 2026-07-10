# TechCup Identity Service — Referee & Organizer Account Creation

**Requirements implemented:** TC-4 (Referee account creation), TC-5 (Admin/Organizer account setup)

---

## Tech Stack

Java 21 · Spring Boot 3.5.6 · Maven · PostgreSQL · Spring Security · Spring Mail · Jacoco

---

## Project Structure

```
src/
├── main/
│   ├── java/co/edu/escuelaing/techcup/identity/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java       # PasswordEncoder bean, @EnableMethodSecurity
│   │   │   └── OrganizerSeeder.java      # Seeds initial Organizer account (TC-5)
│   │   ├── controller/
│   │   │   └── RefereeController.java    # POST /api/v1/referees (TC-4)
│   │   ├── dto/
│   │   │   └── RefereeRequestDTO.java
│   │   ├── entity/
│   │   │   └── IdType.java               # Enum: CC, TI, CE
│   │   ├── exception/
│   │   │   └── BusinessException.java
│   │   └── service/
│   │       ├── UserService.java
│   │       ├── RefereeValidator.java
│   │       ├── TemporaryPasswordGenerator.java
│   │       ├── RandomTemporaryPasswordGenerator.java
│   │       └── FullNameSplitter.java
└── test/
    └── java/co/edu/escuelaing/techcup/identity/
        ├── config/
        │   └── OrganizerSeederTest.java
        └── service/
            ├── UserServiceTest.java
            ├── RefereeValidatorTest.java
            └── FullNameSplitterTest.java
```

---

## Business Rules

### TC-4 — Referee Account Creation

- Only a user authenticated with role `ORGANIZER` can create referee accounts.
- Email must belong to the `@gmail.com` domain (personal account, not institutional).
- ID number must be unique across the system and contain only digits.
- Full name must be between 3 and 100 characters.
- Password is generated temporarily and encrypted before persistence.
- Account is persisted with role `REFEREE` and status `Active`.
- Credential email and OTP code are triggered upon successful creation.

**Endpoint:** `POST /api/v1/referees`
Protected with `@PreAuthorize("hasRole('ORGANIZER')")`.

### TC-5 — Admin/Organizer Account Setup

- Not created through any public registration form or API endpoint.
- Provisioned automatically on application startup via `OrganizerSeeder`.
- Password is encrypted before persistence.
- Does not trigger the OTP validation flow.
- The Organizer role cannot be granted to any other user beyond initial provisioning.

---

## Running locally

```bash
mvn spring-boot:run
```

---

## Running tests

```bash
mvn verify
```

Coverage report is at: `target/site/jacoco/index.html`

| Class | Coverage |
|-------|----------|
| `UserService` | 100% |
| `RefereeValidator` | 100% |
| `FullNameSplitter` | 100% |
| `OrganizerSeeder` | 100% |



src/test/resources/image.png
src/test/resources/image2.png
src/test/resources/image3.png
src/test/resources/image4.png
src/test/resources/image5.png