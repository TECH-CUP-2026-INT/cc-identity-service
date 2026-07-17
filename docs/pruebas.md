# Testing

The CC Identity Service uses the standard Spring Boot testing stack. Tests are executed with Maven.

## Running the tests

```bash
./mvnw test
```

or, after building:

```bash
mvn test
```

## Test layers

| Layer | Scope | Typical tools |
|-------|-------|---------------|
| Unit | Domain models and use cases (no Spring context) | JUnit 5, Mockito |
| Integration | REST controllers and MongoDB adapters (Spring context) | `@SpringBootTest`, `@WebMvcTest`, Testcontainers (MongoDB) |
| Contract | Inter-service contracts (Feign clients) | Spring Cloud Contract / consumer tests |

## Coverage

JaCoCo is configured (`jacoco.version` 0.8.12) to measure test coverage during the build. Generate the report with:

```bash
./mvnw test jacoco:report
```

The HTML report is written to `target/site/jacoco/index.html`.

## Conventions

- Domain logic is covered by fast, isolated unit tests (e.g. `User.registerFailedLoginAttempt`, OTP expiry).
- Controllers are verified through `@WebMvcTest` with mocked use cases.
- The `GlobalExceptionHandler` is tested to assert correct HTTP status mapping for each `DomainException`.

[Back to top](#testing)
