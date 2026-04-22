# agridata.ch Backend

This repository contains the backend services for the agridata.ch platform, built with Quarkus. It provides APIs,
authentication integration, and data persistence capabilities.

---

## Local Development Setup

Local development is powered by **Quarkus DevServices**, which automatically starts required services like PostgreSQL
and Keycloak in Docker containers.

### Getting Started

To launch the application in development mode:

#### RUN

```bash
mvn quarkus:dev -Dquarkus.profile=local
```

This will automatically spin up:

- A PostgreSQL database
- A Keycloak instance with the agate_realm.json imported

## Quarkus Dev UI

Access the Quarkus Developer UI here:

http://localhost:8060/q/dev-ui

## Add git hook folder

```bash
git config --local core.hooksPath hooks && chmod +x hooks/*
```

## Database Configuration

- Database Name: `agridata`
- Username: `admin`
- Password: `secret`
- Data Persistence Path: `.local-dev/postgres-volume`

## Keycloak Configuration

- Realm: `agate`
- Initial Import: `agate_realm.json`
- Admin Credentials:
    - Username: `admin`
    - Password: `admin`
- Client:
    - Id: `agridata-ui`
    - Password: `secret`

## Local Users and Credentials

These are predefined users in the local agate realm:

### Data Producers

| Username     | Password | Roles                            | KT_ID_P   | loginid |
|--------------|----------|----------------------------------|-----------|---------|
| producer-a   | secret   | agridata.ch.Agridata_Einwilliger | FLXXA0001 | 3477580 |
| producer-b   | secret   | agridata.ch.Agridata_Einwilliger | FLXXB0001 | 3477581 |
| producer-b-3 | secret   | agridata.ch.Agridata_Einwilliger | FLXXB0003 | 3477582 |
| producer-c   | secret   | agridata.ch.Agridata_Einwilliger | FLXXC0001 | 3477583 |
| producer-d   | secret   | agridata.ch.Agridata_Einwilliger | FLXXD0001 | 3477584 |
| producer-e   | secret   | agridata.ch.Agridata_Einwilliger | -         | 3477585 |

### Data Providers

| Username   | Password | Roles                               | UID                | loginid |
|------------|----------|-------------------------------------|--------------------|---------|
| provider-1 | secret   | agridata.ch.Agridata_Datenanbieters | CHE146680598 (BLW) | 3477553 |
| provider-2 | secret   | agridata.ch.Agridata_Datenanbieters | CHE146680598 (BLW) | 3477586 |

### Data Consumers

| Username                 | Password | Roles                              | UID                       | loginid  |
|--------------------------|----------|------------------------------------|---------------------------|----------|
| consumer                 | secret   | agridata.ch.Agridata_Datenbezueger | CHE101708094 (Bio Suisse) | 20154600 |
| consumer-ip-suisse       | secret   | agridata.ch.Agridata_Datenbezueger | CHE110013660 (IP Suisse)  | 900000   |
| consumer-blv-1           | secret   | agridata.ch.Agridata_Datenbezueger | CHE403244345 (BLV)        | 3477588  |
| consumer-blv-2           | secret   | agridata.ch.Agridata_Datenbezueger | CHE403244345 (BLV)        | 3477589  |
| consumer-blv-without-uid | secret   | agridata.ch.Agridata_Datenbezueger | -                         | 3477590  |

### Others

| Username | Password | Roles                        | loginid |
|----------|----------|------------------------------|---------|
| admin    | secret   | agridata.ch.Agridata_Admin   | 3477554 |
| support  | secret   | agridata.ch.Agridata_Support | 3477555 |
| guest    | secret   | -                            | -       |

## Managing Keycloak Configuration

User data cannot easily be exported from the Docker container, so manual steps are required to preserve and update user
configuration:

- Backup the `users`section in `agate_realm.json`.
- Log into the Keycloak admin console.
- Go to **Realm Settings > Action > Partial Export**.
- Replace `agridata-ui.secret` in the exported file with `secret`.
- Re-add the `users` section from your backup.
- Replace the original `agate_realm.json` with the updated version.
