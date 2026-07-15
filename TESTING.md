# Pruebas y cobertura

Las pruebas se generaron respetando la estructura hexagonal del servicio:

- `src/test/java/.../domain`: modelos de dominio, validadores y edge cases de reglas puras.
- `src/test/java/.../application/usecase`: casos de uso con puertos mockeados.
- `src/test/java/.../infrastructure/adapter/in/rest`: controladores REST, validación HTTP, errores de serialización y manejo global de excepciones.
- `src/test/java/.../infrastructure/adapter/out`: adaptadores de correo y persistencia con mocks.
- `src/test/java/.../infrastructure/config/security`: filtro JWT y comportamiento de cabeceras `Authorization`.
- `src/test/java/.../infrastructure/openapi`: pruebas Swagger/OpenAPI sobre `/v3/api-docs`, Swagger UI, schemas, request bodies, parámetros y security scheme.
- `src/test/java/.../shared/util`: utilidades JWT, OTP y password.

## Cobertura funcional incluida

La suite incluye pruebas para casos felices, negativos y de frontera en:

- login institucional y login Google;
- OTP válido, expirado, usado, inexistente, incorrecto, alfanumérico, blank, máximo de intentos y cooldown de reenvío;
- recuperación y reset de contraseña con correo inexistente, código inexistente, usado, expirado e incorrecto;
- validación/revocación de JWT;
- logout idempotente e inválido;
- validaciones de DTO con `null`, blank, email inválido y enums inválidos;
- JSON malformado, body vacío, `Content-Type` no soportado y headers `Bearer` malformados;
- auditoría con fechas inválidas, `startDate > endDate` y `actionType` inválido;
- dominios de email exactos para `escuelaing.edu.co` y `gmail.com`, incluyendo mayúsculas, espacios, subdominios y lookalikes;
- Swagger/OpenAPI de endpoints, request schemas, campos requeridos, parámetros de auditoría y security scheme JWT.

## Ejecutar pruebas

```bash
mvn clean test
```

## Ejecutar pruebas con validación de cobertura JaCoCo > 80%

```bash
mvn clean verify
```

El reporte HTML queda en:

```text
target/site/jacoco/index.html
```

El `pom.xml` incluye `jacoco-maven-plugin` con regla mínima de cobertura de líneas `0.80` en fase `verify`.
