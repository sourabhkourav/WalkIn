# WalkIn frontend

The React client provides two experiences:

- `/` — authenticated venue operations and candidate queue management
- `/register/{registrationToken}` — public, drive-specific candidate registration

The public form renders only fields enabled by the hiring drive. Candidate reminder preferences are
collected separately and private notification destinations are not shown in the organizer queue.

## Development

Run from this directory:

```powershell
npm.cmd install
npm.cmd run dev
```

Vite proxies `/api` to `http://localhost:8080`. Start the Spring Boot API and PostgreSQL separately.
See the repository `docs` directory for complete setup, testing, security, and API guidance.

## Verification

```powershell
npm.cmd run lint
npm.cmd test
npm.cmd run build
```

Do not put credentials, registration tokens, or candidate information in frontend environment
files, source code, tests, screenshots, or logs.
