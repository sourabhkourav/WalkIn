# Local development

## Requirements

- JDK 25
- Docker Desktop with Docker Compose
- Node.js and npm for frontend development

Copy the safe template and replace every placeholder with a local-only value:

```powershell
Copy-Item .env.example .env
```

The `.env` file is ignored by Git. Do not copy real production secrets into it.

## Run the backend and database with Docker

```powershell
docker compose up --build
```

Stop the containers without deleting PostgreSQL data:

```powershell
docker compose down
```

The `postgres-data` volume preserves local data. Adding `--volumes` deletes that data and should be
used only when a clean database is intentional.

## Run Spring Boot on the host

Start PostgreSQL first, then run the Maven Wrapper from the repository root:

```powershell
docker compose up -d postgres
.\mvnw.cmd spring-boot:run
```

Confirm that both `java -version` and `.\mvnw.cmd -version` report Java 25 if compilation reports an
unsupported release or class-file version.

## Run the frontend

From `frontend`:

```powershell
npm.cmd install
npm.cmd run dev
```

`frontend/.env.example` documents the optional API base URL. Leave it empty when the frontend uses
the Vite development proxy or shares an origin with the API.

After signing in, the organizer dashboard selects the newest hiring drive returned by the API.
Create and open a drive, then register a synthetic candidate through its public token to exercise
queue filtering and status actions. The dashboard intentionally does not display the candidate's
notification destination.

To exercise the candidate experience, open `/register/{registrationToken}` on the frontend origin,
using the one-time raw token returned when the drive was created. In production, the web server must
route this deep link to the frontend `index.html`. Use only synthetic candidate data locally.
