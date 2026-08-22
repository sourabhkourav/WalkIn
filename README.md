# WalkIn

WalkIn is a Spring Boot REST API for managing recruitment walk-in drives. It tracks students,
companies, interview rounds, applications, and round-by-round selection decisions.

## Technology

- Java 25 LTS
- Spring Boot 4.1
- PostgreSQL 18
- Spring Data JPA and Hibernate
- Flyway database migrations
- Spring Security with stateless HTTP Basic authentication
- Maven Wrapper
- H2 for isolated automated tests

## Run locally

Requirements: JDK 25 and Docker Desktop (or a separately managed PostgreSQL 18 server).

1. Create your local environment file:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Replace every placeholder password in `.env`. The file is ignored by Git and is read by both
   Docker Compose and Spring Boot.

3. Start PostgreSQL:

   ```powershell
   docker compose up -d
   ```

4. Start the API:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. Authenticate API calls with the `APP_SECURITY_USERNAME` and `APP_SECURITY_PASSWORD` values
   from `.env`.

## Test

Tests use an in-memory H2 database in PostgreSQL compatibility mode. A local database and `.env`
file are not required.

```powershell
.\mvnw.cmd test
```

## REST endpoints

All endpoints require HTTP Basic authentication.

| Resource | Endpoint |
| --- | --- |
| Students | `/api/students` |
| Companies | `/api/companies` |
| Interview round definitions | `/api/interview-rounds` |
| Rounds assigned to companies | `/api/company-rounds` |
| Student applications | `/api/applications` |
| Student round decisions | `/api/round-selections` |

Each resource supports `POST`, `GET` collection, `GET /{id}`, `PUT /{id}`, and `DELETE /{id}`.
Relationship requests use IDs. For example, assigning a round to a company uses:

```json
{
  "companyId": 1,
  "interviewRoundId": 1
}
```

Schema changes belong in versioned scripts under `src/main/resources/db/migration`. Hibernate
validates the mapped entities against that schema at startup; it does not modify production tables.

## Secret handling

Never commit `.env` or real credentials. If a real credential is committed, rotate it immediately;
removing it from the current file or rewriting Git history does not invalidate the exposed value.
