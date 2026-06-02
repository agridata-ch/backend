# agridata.ch Backend

This repository contains the backend services for the agridata.ch platform, built with Quarkus. It handles agricultural
data exchange between producers, consumers, and data providers — including consent flows, data transfer pipelines, and
integrations with external registries (AGIS, TVD, UID Register, BIT).

---

## Prerequisites

- Java 25
- Maven 3.9.11 (or use the included `./mvnw` wrapper)
- Docker Desktop (required for DevServices — PostgreSQL, Keycloak, LocalStack, WireMock)

---

## Getting Started

### 1. Register git hooks

```bash
git config --local core.hooksPath hooks && chmod +x hooks/*
```

### 2. Start the application

```bash
mvn quarkus:dev -Dquarkus.profile=local
```

> **Windows:** Ensure Docker Desktop is running before executing this command.
>
> **Windows PowerShell:** If you encounter `unknown lifecycle phase ".profile=local"`, use:
> ```powershell
> mvn quarkus:dev --define quarkus.profile=local
> ```

DevServices automatically starts:

| Service    | Port | Purpose                      |
|------------|------|------------------------------|
| PostgreSQL | 5432 | Primary database             |
| Keycloak   | 6999 | OIDC / authentication        |
| LocalStack | 4566 | S3, SNS, SES (AWS emulation) |
| WireMock   | 8050 | External API stubs           |

### Useful local URLs

| URL                                | Description               |
|------------------------------------|---------------------------|
| http://localhost:8060/q/dev-ui     | Quarkus Developer UI      |
| http://localhost:8060/q/swagger-ui | Swagger UI (multi-subset) |
| http://localhost:6999              | Keycloak admin console    |

---

## Running Tests

```bash
# Unit tests only
mvn test

# Unit + integration tests
mvn verify
```

> **Integration tests** require Docker Desktop — DevServices spins up a full Quarkus stack with DB and Keycloak.
> Unit tests have no infrastructure dependency.

---

## Configuration

### Database

- **Name:** `agridata`
- **Username:** `admin`
- **Password:** `secret`
- **Data Persistence Path:** `.local-dev/postgres-volume`

### Keycloak

- **Realm:** `agate`
- **Initial Import:** `agate_realm.json`
- **Admin Credentials:** `admin` / `admin`
- **Client:** `agridata-ui` / `secret`

#### Updating the Keycloak realm config

User data cannot easily be exported from the Docker container, so manual steps are required:

1. Backup the `users` section in `agate_realm.json`.
2. Log into the Keycloak admin console at http://localhost:6999.
3. Go to **Realm Settings > Action > Partial Export**.
4. Replace `agridata-ui.secret` in the exported file with `secret`.
5. Re-add the `users` section from your backup.
6. Replace the original `agate_realm.json` with the updated version.

---

## Local Users

These are predefined users in the local `agate` realm. All passwords are `secret`.

### Data Producers

Role: `agridata.ch.Agridata_Einwilliger`

| Username     | KT_ID_P                | loginid |
|--------------|------------------------|---------|
| producer-a   | FLXXA0001              | 3477580 |
| producer-b   | FLXXB0001              | 3477581 |
| producer-b-3 | FLXXB0003              | 3477582 |
| producer-c   | FLXXC0001              | 3477583 |
| producer-d   | FLXXD0001              | 3477584 |
| producer-e   | - (no farm registered) | 3477585 |

### Data Providers

Role: `agridata.ch.Agridata_Datenanbieter`

| Username   | UID                | loginid |
|------------|--------------------|---------|
| provider-1 | CHE146680598 (BLW) | 3477553 |
| provider-2 | CHE146680598 (BLW) | 3477586 |

### Data Consumers

Role: `agridata.ch.Agridata_Datenbezueger`

| Username                 | UID                       | loginid  |
|--------------------------|---------------------------|----------|
| consumer                 | CHE101708094 (Bio Suisse) | 20154600 |
| consumer-ip-suisse       | CHE110013660 (IP Suisse)  | 900000   |
| consumer-blv-1           | CHE403244345 (BLV)        | 3477588  |
| consumer-blv-2           | CHE403244345 (BLV)        | 3477589  |
| consumer-blv-without-uid | -                         | 3477590  |

### Other Users

| Username | Role                         | loginid |
|----------|------------------------------|---------|
| admin    | agridata.ch.Agridata_Admin   | 3477554 |
| support  | agridata.ch.Agridata_Support | 3477555 |
| guest    | -                            | -       |

---

## IDE Setup

The project targets **Java 25** and uses **Lombok** for code generation. Configure your IDE before building.

---

## Troubleshooting

### `unknown lifecycle phase ".profile=local"` (Windows PowerShell)

PowerShell interprets the `.` in `-Dquarkus.profile=local` as a property accessor. Use `--define` instead:

```powershell
mvn quarkus:dev --define quarkus.profile=local
```

### Docker not running

All DevServices (PostgreSQL, Keycloak, LocalStack, WireMock) require Docker. Start Docker Desktop before running
`mvn quarkus:dev` or `mvn verify`.
