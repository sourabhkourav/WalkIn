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
| Application users | `/api/users` |

General resources support create, get, list, update, and delete operations. Student, company, and
interview-round lists accept `page`, `size`, `sort`, `direction`, and `query`; page size is limited
to 100.

`ADMIN` can read and modify resource APIs. `RECRUITER` can read them. Only `ADMIN` can manage users,
including roles, enabled status, and password resets. Password hashes are never returned by the API,
and the last enabled administrator cannot be disabled or demoted.

Candidate reporting schedules currently have persistence support but do not yet have a public API.

