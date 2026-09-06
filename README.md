# WalkIn

**Helping offline hiring drives run on time.**

WalkIn is a venue operations platform for offline recruitment drives. It helps hiring teams manage
candidates and interview rounds while giving candidates enough freedom to leave the waiting area
when their interview is not immediate.

Candidates record how early they want to be contacted and which notification channel they prefer.
Venue staff can then coordinate reporting times and track progress through each interview round.

## Why WalkIn

Offline hiring drives often involve long, uncertain waiting periods. Candidates may miss meals or
remain near an interview room because they do not know when they will be called. At the same time,
venue coordinators need one reliable view of candidates, companies, applications, and round results.

WalkIn is designed to make that flow predictable: candidates report at an assigned time, and staff
manage the drive from a single system.

## How it works

1. A hiring company creates a drive, chooses registration fields, and orders its interview rounds.
2. WalkIn generates a public registration link and venue-ready QR code.
3. Candidates scan the QR code, submit the requested details, and privately choose how early and
   through which channel they want to be notified.
4. Recruiters manage the live queue while candidates remain available at their specified time.
5. Staff record progress through the recruitment process without seeing another company's data.

## Core features

- Candidate registration, search, pagination, editing, and deletion
- Company, application, and interview-round management
- Candidate notification-channel and advance-notice preferences
- Interview reporting-time and round-status persistence
- Candidate selection and recruitment-progress tracking
- Tenant-isolated access for platform administrators, company administrators, and recruiters
- Hiring-drive setup with configurable forms, ordered rounds, public links, and QR codes

## Product status

WalkIn is under active development. Candidate management and the supporting recruitment APIs are
available. Reporting schedules are persisted, while automated SMS, email, and WhatsApp delivery is
planned and is not yet active.

## Technology

WalkIn uses Java 25, Spring Boot, React, PostgreSQL, Flyway, and Docker. Authentication uses
short-lived JWT access tokens, role-based authorization, and BCrypt password hashing. The automated
test suite uses JUnit, Mockito, Vitest, and Testcontainers.

## Quick start

Docker Desktop is the simplest way to run the backend and database locally:

```powershell
Copy-Item .env.example .env
# Replace every placeholder in .env before continuing.
docker compose up --build
```

The API is available at `http://localhost:8080`, and Swagger UI is available at
`http://localhost:8080/swagger-ui.html`. Never commit `.env` or use development credentials in a
deployed environment.

## Documentation

- [Documentation guide](docs/README.md)
- [Architecture](docs/architecture.md)
- [Local development](docs/development.md)
- [API guide](docs/api.md)
- [Database and migrations](docs/database.md)
- [Testing](docs/testing.md)
- [Security](docs/security.md)
- [Operations](docs/operations.md)
