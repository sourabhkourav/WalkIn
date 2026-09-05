# API guide

The API base URL is `http://localhost:8080` during local development. OpenAPI documentation is
available at `/swagger-ui.html`, and the machine-readable document is at `/v3/api-docs`.

## Authentication

Obtain a short-lived token using the administrator values configured in your local `.env` file:

```http
POST /api/auth/login
Content-Type: application/json

{"username":"<local-admin-username>","password":"<local-admin-password>"}
```

Send the returned token on protected requests:

```http
Authorization: Bearer <access-token>
```

Do not paste real tokens into documentation, issues, screenshots, or chat messages.

## Resources

| Resource | Endpoint |
| --- | --- |
| Students | `/api/students` |
| Companies | `/api/companies` |
| Interview-round definitions | `/api/interview-rounds` |
| Rounds assigned to companies | `/api/company-rounds` |
| Student applications | `/api/applications` |
| Student round decisions | `/api/round-selections` |
| Candidate reporting schedules | `/api/candidate-round-schedules` |
| Hiring drives | `/api/hiring-drives` |
| Ordered rounds in a hiring drive | `/api/hiring-drives/{driveId}/rounds` |
| Application users | `/api/users` |

General resources support create, get, list, update, and delete operations. Student, company, and
interview-round lists accept `page`, `size`, `sort`, `direction`, and `query`; page size is limited
to 100.

`ADMIN` can read and modify resource APIs. `RECRUITER` can read them. Only `ADMIN` can manage users,
including roles, enabled status, and password resets. Password hashes are never returned by the API,
and the last enabled administrator cannot be disabled or demoted.

Candidate reporting schedules support creation, retrieval by ID, and paginated listing. Schedule
responses contain relationship IDs instead of nested candidate and company records, preventing
unrelated personal or business data from being exposed through this resource.

Administrators can reschedule a future reporting time with
`PUT /api/candidate-round-schedules/{id}/reporting-time` while the schedule is still `SCHEDULED`.
They can advance its lifecycle with `PATCH /api/candidate-round-schedules/{id}/status`.

```text
SCHEDULED -> NOTIFIED -> REPORTED
     |           |
     +-----------+----> MISSED
     +-----------+----> CANCELLED
```

`REPORTED`, `MISSED`, and `CANCELLED` are terminal states. Moving to `NOTIFIED` records
`notifiedAt`; moving to `REPORTED` records `reportedAt`. Repeating the current status is safe and
does not replace the original timestamp. Invalid lifecycle transitions return HTTP `409 Conflict`.

## Due notifications

Authenticated administrators and recruiters can inspect the next due notification batch with
`GET /api/candidate-round-schedules/due`. A schedule becomes due when the current time reaches its
reporting time minus the candidate's chosen advance-notice minutes. Only future `SCHEDULED` entries
are returned, ordered by reporting time, with a maximum batch size of 100.

The response contains IDs, timing, and the selected channel, but excludes email addresses, phone
numbers, resumes, and other candidate details. This endpoint detects due work only: it does not send
a message or change the schedule to `NOTIFIED`.

## Hiring drives

Administrators create drives with `POST /api/hiring-drives` and receive the public registration
token once in the creation response. Later authenticated reads never return the token or its stored
hash. Administrators change lifecycle state with `PATCH /api/hiring-drives/{id}/status`; recruiters
have read-only access.

An unauthenticated candidate client can resolve an open, unexpired drive with
`GET /api/public/hiring-drives/{registrationToken}`. The public response contains only the company
name, drive name, venue, and operating times. It excludes internal IDs, company contact details,
token metadata, and all candidate information. Candidate registration submission is not available
yet.

Administrators assign a reusable company round to a drive with
`POST /api/hiring-drives/{driveId}/rounds`. Each assignment has a positive `roundOrder`, and the
same position or company round cannot be used twice in one drive. A round must belong to the same
company as the drive. Assignments are allowed while the drive is `DRAFT` or `OPEN`, supporting
rounds added at the venue while hiring is underway. Administrators and recruiters can retrieve the
ordered plan with `GET /api/hiring-drives/{driveId}/rounds`.

## Registration forms

Administrators configure the standard fields requested by a drive with
`PUT /api/hiring-drives/{id}/registration-form`. Each field accepts `HIDDEN`, `OPTIONAL`, or
`REQUIRED`:

```json
{
  "firstName": "REQUIRED",
  "lastName": "OPTIONAL",
  "email": "REQUIRED",
  "contactNumber": "HIDDEN",
  "resume": "OPTIONAL"
}
```

The configuration can change only while the drive is `DRAFT`, preventing candidates from seeing
different forms after registration opens. First name must remain visible, and at least one of email
or contact number must be visible. Recruiters can read the configuration but cannot modify it.

The public hiring-drive response includes the same configuration under `registrationForm`, allowing
the candidate UI to render the appropriate fields after a QR link is opened. Notification channel
and advance-notice choices remain a separate candidate-controlled step and are not part of the
company's field configuration. Arbitrary custom questions and public submission are not supported
yet.
