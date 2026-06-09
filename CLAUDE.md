# CLAUDE.md – agridata.ch Backend

Quarkus 3.36.0 + Java 25 backend for the agridata.ch agricultural data-exchange platform. Uses PostgreSQL (Flyway + Hibernate ORM Panache),
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

All business code lives under `ch.agridata.<module>`. Modules: `agreement`, `user`, `auditing`, `agis`, `aws`, `bit`, `common`,
`datatransfer`,
`datatransferv2`, `notification`, `product`, `testdata`, `uidregister`, `tvd`.

Each module follows a strict layered layout enforced by ArchUnit (`ModuleArchitectureTest`):

```
controller  →  service  →  persistence
               ↓
               api        (cross-module contract interface)
               mapper     (MapStruct, componentModel="jakarta")
               dto
```

**Cross-module rules (violations fail CI):**

- Only `service` layers may call another module's `api` or `service`.
- DTOs may be referenced from `service`, `dto`, `controller`, `mapper` layers.
- Only whitelisted `common` sub-packages may be imported by other modules: `common.persistence`, `common.api`, `common.dto`, `common.utils`,
  `common.security`, `common.exceptions`, `common.filters`, `common.openapi`.

---

## Key Conventions

### Entities

- All entities extend `AuditableEntity` (`createdBy`, `createdAt`, `modifiedBy`, `modifiedAt`, `archived`).
- Soft-delete via `@SQLDelete(sql = "UPDATE … SET archived = true …")` + `@SQLRestriction("archived = false")`.
- Multilingual text stored as PostgreSQL JSON using `TranslationPersistenceDto(String de, String fr, String it)` with
  `@JdbcTypeCode(SqlTypes.JSON)`.

### Controllers

- All controllers annotated `@RunOnVirtualThread`.
- Every endpoint method with `@Operation` **must** carry `@ApiSubset({…})` (enforced by `ApiSubsetArchitectureTest`).  
  Available subset constants: `WEB_APP`, `MOBILE_APP`, `DATA_CONSUMER`, `DATA_PROVIDER` (see `ApiSubsetConstants`).

### Javadoc

- Every class must have a Javadoc comment ending with `@CommentLastReviewed <date>`, that secures comments from being copied. Does not need
  to be consistently updated.   
  Example: `* @CommentLastReviewed 2026-02-16`

### Roles

Defined in `AuthenticationUtil`:

| Constant        | Keycloak Role                        |
|-----------------|--------------------------------------|
| `PRODUCER_ROLE` | `agridata.ch.Agridata_Einwilliger`   |
| `CONSUMER_ROLE` | `agridata.ch.Agridata_Datenbezueger` |
| `PROVIDER_ROLE` | `agridata.ch.Agridata_Datenanbieter` |
| `ADMIN_ROLE`    | `agridata.ch.Agridata_Admin`         |
| `SUPPORT_ROLE`  | `agridata.ch.Agridata_Support`       |

User identity comes from the Agate `loginid` JWT claim, converted to a UUID v3 via `AgridataSecurityIdentity.getUserId()`. Support users can
impersonate via the `X-Impersonated-AgateLoginId` request header.

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

`datatransferv2` uses a pipeline pattern. Each flow implements `Flowable` and calls `AgridataFlow.run(context, tasksBefore, tasksAfter)`.
Tasks are `UnaryOperator<AgridataContext>` beans composed into lists. The four flows are:

- `UidBasedPreValidationFlow` – consent checked before upstream call
- `BurBasedPreValidationFlow` – BUR-based consent checked before upstream call
- `BurBasedPostValidationFlow` – BUR-based consent checked after response header
- `UidBasedPostValidationFlow` – UID resolved from response header
- `UnboundPostValidationFlow` – no producer identity constraint before call

---

## External Integrations

| System             | Protocol                                                | Config key                                       |
|--------------------|---------------------------------------------------------|--------------------------------------------------|
| AGIS Register API  | REST (OpenAPI-generated DTOs in `ch.agridata.agis.dto`) | `quarkus.rest-client.agis-api.url`               |
| UID Register       | SOAP/CXF (`uid-register.wsdl`)                          | `quarkus.cxf.client.uid`                         |
| TVD Animal Tracing | REST (OIDC client)                                      | `quarkus.rest-client.tvd-animal-tracing-api.url` |
| BIT Signature API  | REST (mTLS PKCS12)                                      | `quarkus.rest-client.bit-signature-api.url`      |
| AControl API       | REST (mTLS PKCS12)                                      | `quarkus.rest-client.acontrol-api.url`           |
| Data Providers     | REST (`DataProviderRestClient`)                         | Per-product configuration                        |

WireMock stubs for all external calls live in `src/test/resources/wiremock/`.

---

## Java Code style

- Strive for simplicity and clean code, not cleverness. Always prefer readability to brevity.
- Use `var` for local variables with obvious types, otherwise explicit types.
- Use a single empty new-line at the end of all files, never 2.
