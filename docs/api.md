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
