# CLAUDE.md – agridata.ch Backend

Quarkus + Java 25 backend for the agridata.ch agricultural data-exchange platform. Uses PostgreSQL (Flyway + Hibernate ORM Panache),
Keycloak OIDC, MapStruct, Lombok, WireMock, and ArchUnit.

---

## Developer Workflows

```bash
# Start dev mode (auto-starts PostgreSQL + Keycloak via DevServices Docker containers)
mvn quarkus:dev -Dquarkus.profile=local

# Run unit tests only
mvn test

# Run unit + integration tests
mvn verify

# Quarkus Dev UI
http://localhost:8060/q/dev-ui

# Swagger UI (multi-subset)
http://localhost:8060/q/swagger-ui
```

Register git hooks after cloning: `git config --local core.hooksPath hooks && chmod +x hooks/*`

---

## Module Structure

All business code lives under `ch.agridata.<module>` (e.g. `agreement`, `datatransferv2`, `notification`, `common`).

Each module follows a strict layered layout enforced by ArchUnit (`ModuleArchitectureTest`):

```
controller  →  service  →  persistence
               ↓
               api        (cross-module contract interface)
               mapper     (MapStruct, componentModel="jakarta")
               dto
```

**Cross-module rules (violations fail CI):**

- Is enforced by `ModuleArchitectureTest`
    - Only `service` layers may call another module's `api` or `service`.
    - DTOs may be referenced from `service`, `dto`, `controller`, `mapper` layers.

---

## Key Conventions

### Entities

- All entities extend `AuditableEntity` (`createdBy`, `createdAt`, `modifiedBy`, `modifiedAt`, `archived`).
- Soft-delete via `@SQLDelete(sql = "UPDATE … SET archived = true …")` + `@SQLRestriction("archived = false")`.
- Multilingual text stored as PostgreSQL JSON using `TranslationPersistenceDto(String de, String fr, String it)` with
  `@JdbcTypeCode(SqlTypes.JSON)`.

### Controllers

- Business controllers are annotated `@RunOnVirtualThread`
- Every endpoint method with `@Operation` **must** carry `@ApiSubset({…})` (enforced by `ApiSubsetArchitectureTest`).  
  Available subset constants: `WEB_APP`, `MOBILE_APP`, `DATA_CONSUMER`, `DATA_PROVIDER` (see `ApiSubsetConstants`).

### Javadoc

- Every class must have a Javadoc comment ending with `@CommentLastReviewed <date>` (e.g. `2026-02-16`).

### Roles & Identity

- Role constants (`PRODUCER_ROLE`, `CONSUMER_ROLE`, `PROVIDER_ROLE`, `ADMIN_ROLE`, `SUPPORT_ROLE`) map to Keycloak
  `agridata.ch.Agridata_*` roles in `AuthenticationUtil`.
- User identity comes from the Agate `loginid` JWT claim, converted to a UUID v3 via `AgridataSecurityIdentity.getUserId()`.
  Support users impersonate via the `X-Impersonated-AgateLoginId` request header.

### Flyway Migrations

Naming pattern: `V{YEAR}.{MM}.{DD}_{TICKET}__description.sql`  
Example: `V2026.02.16_466__add_uid_field_to_data_provider_table.sql`

---

## Testing

### Two Test Categories

| Type        | Package         | Annotation     | Infrastructure               |
|-------------|-----------------|----------------|------------------------------|
| Unit        | `ch.agridata.*` | Plain JUnit 5  | None                         |
| Integration | `integration.*` | `@QuarkusTest` | Full Quarkus + DB + Keycloak |

`@QuarkusTest` classes **outside** `integration.*` will fail `ModuleArchitectureTest`.

### Integration Test Patterns

- Call `flyway.migrate()` in `@BeforeEach` to reset the DB to the known test-data state.
- External services (AGIS, TVD, UID Register) are stubbed with WireMock; inject `WireMock wireMock` and annotate the class with
  `@ConnectWireMock`. Call `wireMock.resetToDefaultMappings()` in `@BeforeEach`.
- Authenticated HTTP: `AuthTestUtils.requestAs(TestUserEnum.CONSUMER_BIO_SUISSE).when().get(…)`.
- Access-control tests: `AccessTestUtils.assertForbiddenForAllExcept(GET, path, PRODUCER_ROLE, ADMIN_ROLE)`.
- Type-safe test fixture IDs live in `TestDataIdentifiers` (e.g., `TestDataIdentifiers.DataRequest.BIO_SUISSE_01`).

---

## Data Transfer Flow (v2)

- `datatransferv2` uses a pipeline pattern. Each flow implements `Flowable` and calls `AgridataFlow.run(context, tasksBefore, tasksAfter)`.
- Tasks are `UnaryOperator<AgridataContext>` beans composed into lists.
- The five flows: `UidBasedPreValidationFlow`, `BurBasedPreValidationFlow`, `BurBasedPostValidationFlow`, `UidBasedPostValidationFlow`,
  `UnboundPostValidationFlow`(pre/post = consent checked before/after the upstream call; UID/BUR = how producer identity is resolved).

---

## External Integrations

- Most external systems (AGIS, UID Register, TVD Animal Tracing, TVD ZO, BIT Signature, AControl, Data Providers) are REST clients
  configured under `quarkus.rest-client.*` in `application.yml`. Only UID Register uses REST client to access a SOAP-API.
    - BIT and AControl use mTLS (PKCS12).
    - AGIS DTOs are OpenAPI-generated in `ch.agridata.agis.dto`.
    - WireMock stubs for all external calls live in `src/test/resources/wiremock/`.

---

## Code Style

- Use `var` for local variables with obvious types, otherwise explicit types.
- End every file with a single trailing newline, never two.
- Run single tests (`mvn test -Dtest=ClassName`) rather than the whole suite while iterating.

---

## Working Conventions

- Think before coding
    - Don't assume, ask instead
    - Don't hide confusion
    - Surface tradeoffs
- Strive for simplicity and clean code
    - Prefer readability over cleverness
- Write the minimum code that solves the task and matches surrounding style.
    - Keep changes surgical: edit only what the task requires.
