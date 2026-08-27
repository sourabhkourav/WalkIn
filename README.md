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
- JUnit and Mockito for automated tests
- H2 for isolated integration tests
- Docker and Docker Compose

## Run with Docker

Docker Desktop is the only requirement for running the complete stack.

1. Create your local environment file:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Replace every placeholder password in `.env`. The file is ignored by Git and is read by both
   Docker Compose and Spring Boot.

3. Build and start the API and PostgreSQL:

   ```powershell
   docker compose up --build
   ```

4. The API is available at `http://localhost:8080`. Authenticate with the
   `APP_SECURITY_USERNAME` and `APP_SECURITY_PASSWORD` values from `.env`.

5. Stop the stack without deleting database data:

   ```powershell
   docker compose down
   ```

PostgreSQL data is stored in the named `postgres-data` volume. Run
`docker compose down --volumes` only when you intentionally want to delete local database data.

## Run for local development

Install JDK 25, create `.env` as described above, then start only PostgreSQL and run Spring Boot
on the host:

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

## Test

Service unit tests use JUnit and Mockito. Integration tests use an in-memory H2 database in
PostgreSQL compatibility mode. A local database, Docker, and `.env` file are not required.

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
