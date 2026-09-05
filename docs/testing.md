# Testing

## Backend

Run the backend suite from the repository root:

```powershell
.\mvnw.cmd test
```

Run the full Maven lifecycle used by continuous integration:

```powershell
.\mvnw.cmd verify
```

JUnit and Mockito cover services, controllers, validation, authorization, and error handling. H2
provides isolated application tests. Testcontainers verifies Flyway migrations, constraints, and JPA
mappings against PostgreSQL when Docker is available.

## Frontend

Run these commands from `frontend`:

```powershell
npm.cmd test
npm.cmd run lint
npm.cmd run build
```

Vitest and Testing Library verify authentication, queue API calls, drive selection, filtering,
status actions, privacy-safe rendering, forms, and responsive candidate views. Tests must use
synthetic candidate information and fake credentials.

## Continuous integration

GitHub Actions runs Maven verification on Java 25, validates Docker Compose, and builds the
production image for pushes and pull requests. The workflow receives only non-production test
values and has read-only repository permissions.
