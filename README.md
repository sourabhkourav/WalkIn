# WalkIn

WalkIn is a Spring Boot REST API for managing recruitment walk-in drives. It tracks students,
companies, interview rounds, applications, and round-by-round selection decisions.

## Technology

- Java 25 LTS
- Spring Boot 4.1
- PostgreSQL 18
- Spring Data JPA and Hibernate
- Flyway database migrations
- Spring Security with stateless JWT authentication and BCrypt password hashing
- Maven Wrapper
- JUnit and Mockito for automated tests
- H2 for isolated integration tests
- Docker and Docker Compose
- OpenAPI 3 and Swagger UI
- Actuator health probes, Prometheus metrics, and structured production logs

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

4. The API is available at `http://localhost:8080`. Obtain a token from `POST /api/auth/login`
   using the `APP_ADMIN_USERNAME` and `APP_ADMIN_PASSWORD` values from `.env`.

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

## Continuous integration

GitHub Actions runs the complete Maven verification suite on Java 25 for pushes and pull requests.
The Linux runner provides Docker, so the PostgreSQL Testcontainers test runs instead of being
skipped. CI also validates `compose.yaml` and builds the production image without publishing it.
Failed test reports are retained as workflow artifacts for seven days. Dependabot checks Maven,
GitHub Actions, and Docker dependencies weekly.

## Operations

The public health endpoints are `/actuator/health`, `/actuator/health/liveness`, and
`/actuator/health/readiness`; component details are not exposed. `/actuator/prometheus` requires an
`ADMIN` bearer token. Docker Compose enables the `prod` profile, uses the readiness endpoint for
its application health check, emits ECS-formatted JSON logs, and enables graceful shutdown.
Production deployments should terminate HTTPS at the ingress or load balancer, inject credentials
from the platform's secret manager, and configure automated PostgreSQL backups with restore tests;
these infrastructure responsibilities are intentionally not embedded in the application image.

## REST endpoints

All resource endpoints require a JWT bearer token. `ADMIN` can read and modify resources;
`RECRUITER` can read them. Obtain a short-lived access token with:

```http
POST /api/auth/login
Content-Type: application/json

{"username":"walkin-admin","password":"your-password"}
```

| Resource | Endpoint |
| --- | --- |
| Students | `/api/students` |
| Companies | `/api/companies` |
| Interview round definitions | `/api/interview-rounds` |
| Rounds assigned to companies | `/api/company-rounds` |
| Student applications | `/api/applications` |
| Student round decisions | `/api/round-selections` |
| Application users (admin only) | `/api/users` |

Each resource supports `POST`, `GET` collection, `GET /{id}`, `PUT /{id}`, and `DELETE /{id}`.
Student, company, and interview-round collections accept `page`, `size` (maximum 100), `sort`,
`direction`, and `query` parameters. Collection responses include content and page metadata.

Administrators can create users, list users, change roles or enabled status, and reset passwords
through `/api/users`. Password hashes are never included in API responses, and the last enabled
administrator cannot be disabled or demoted.
Relationship requests use IDs. For example, assigning a round to a company uses:

```json
{
  "companyId": 1,
  "interviewRoundId": 1
}
```

Schema changes belong in versioned scripts under `src/main/resources/db/migration`. Hibernate
validates the mapped entities against that schema at startup; it does not modify production tables.

## API documentation

With the application running, open `http://localhost:8080/swagger-ui.html` for interactive API
documentation. Log in through `/api/auth/login`, select **Authorize**, and paste the returned JWT
before trying protected operations. The OpenAPI JSON is at `http://localhost:8080/v3/api-docs`.

## What to build next

Work through these items in order:

1. **Completed:** Add JUnit and Mockito tests for company, interview-round, application, company-round, and
   round-selection services and controllers. Cover validation, missing records, duplicate data,
   and relationship errors.
2. **Completed:** Add Testcontainers integration tests against PostgreSQL so Flyway migrations, constraints, and
   JPA mappings are verified on the same database engine used in production.
3. **Completed:** Add OpenAPI documentation and Swagger UI, including authentication setup.
4. **Completed:** Replace the single HTTP Basic account with application users, BCrypt password hashes, roles,
   and token-based authentication.
5. **Completed:** Add pagination, sorting, filtering, and database uniqueness constraints to collection APIs.
6. **Completed:** Add CI that runs the Maven verification suite and builds the Docker image for every pull request.
7. **Completed (application):** Add Actuator health checks, structured logs, metrics, production
   defaults, and proxy-aware HTTPS handling. HTTPS termination, backups, and secret-manager wiring
   belong to the target deployment platform.

The immediate next milestone is a frontend client for the WalkIn APIs.

## Secret handling

Never commit `.env` or real credentials. If a real credential is committed, rotate it immediately;
removing it from the current file or rewriting Git history does not invalidate the exposed value.
