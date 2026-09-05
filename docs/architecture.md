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

## Hiring-drive model

A hiring drive belongs to a company and contains its venue, operating window, lifecycle status, and
public-registration credential. New drives begin in `DRAFT`; only `OPEN` drives may be resolved by a
public registration token. `CLOSED` and `CANCELLED` are terminal states.

The raw registration token is returned only when a drive is created. The database stores its
SHA-256 hash, following the same principle used for password-reset and API-key lookup systems. QR
codes will eventually contain a registration URL carrying the raw token, never company or candidate
data directly.

A company-round definition is reusable across multiple drives. `HiringDriveRound` is the explicit
assignment between a drive and one of its company's rounds, and stores that round's position in the
drive. This keeps the reusable round definition separate from the event-specific order. Database
constraints prevent a round or order position from being assigned twice within one drive, while the
service layer also enforces company ownership and drive lifecycle rules.

## Registration-form model

Each hiring drive stores an allowlisted requirement for first name, last name, email, contact
number, and resume. A requirement is `HIDDEN`, `OPTIONAL`, or `REQUIRED`. Using known fields keeps
validation and data handling predictable before custom questions are introduced. Configuration is
locked when the drive leaves `DRAFT`, so the public form contract remains stable while candidates
are registering.

Company-requested fields and candidate notification preferences are separate concepts. The company
controls the registration fields; the candidate later chooses the notification channel and advance
notice used to return to the venue on time.

## Candidate registration

`CandidateRegistration` is drive-specific and deliberately separate from the older global student
record. It stores the fields accepted by that drive, the candidate's notification preference, an
opaque public reference, and a queue-oriented status beginning at `WAITING`. This permits the same
person to attend multiple hiring drives without treating one global candidate record as the event
registration.

The public multipart endpoint first resolves the hashed, open-drive token, then enforces the form
configuration. Resume bytes are accepted only for a configured resume field after size, media-type,
and PDF-signature checks. The acknowledgement DTO excludes all submitted personal data.

## Venue candidate queue

Authenticated venue operators manage drive registrations through a separate controller and DTO.
Queue responses contain the fields requested by the company and only indicate whether a resume is
available. They deliberately exclude the candidate-controlled notification destination.

The queue lifecycle uses allowlisted transitions rather than accepting arbitrary state changes.
Every real transition records its time and authenticated operator. A JPA version column protects
against lost updates when multiple operators act on the same candidate at once.
