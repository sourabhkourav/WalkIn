# Architecture

WalkIn has a React frontend, a Spring Boot REST API, and a PostgreSQL database.

```text
Browser (React + Vite)
        |
        | HTTPS / JSON
        v
Spring Boot REST API
  controller -> service -> repository
        |
        | JPA / Hibernate
        v
PostgreSQL <- Flyway migrations
```

## Backend structure

- `controller` defines HTTP endpoints and request validation.
- `service` contains business rules and coordinates related records.
- `repository` provides Spring Data JPA persistence.
- `entity` maps the domain model to PostgreSQL tables.
- `dto` defines API-specific request and response shapes where needed.
- `security` loads application users and creates the bootstrap administrator.
- `exception` converts expected failures into consistent API errors.
- `db/migration` contains the ordered Flyway schema history.

## Frontend structure

- `frontend/src/api` contains HTTP clients.
- `frontend/src/auth` owns the authenticated browser session.
- `frontend/src/components` contains forms and candidate views.
- Component and API tests live beside the code they verify.

## Authentication flow

The client submits credentials to `/api/auth/login`. After authentication, the API returns a
short-lived bearer token. The client sends that token in the `Authorization` header for protected
requests. The server is stateless and does not create an HTTP session.

Administrators can read and modify resources. Recruiters have read-only access to general resource
APIs and cannot access application-user management.

