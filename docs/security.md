# Security

Security is part of every WalkIn change because the system processes candidate contact information
and recruitment decisions.

## Current controls

- Stateless JWT authentication with a configured issuer and short token lifetime
- Role-based authorization for administrators and recruiters
- BCrypt-compatible delegated password hashing
- Environment-based database, bootstrap-user, and signing-key configuration
- No stack traces or internal exception messages in API responses
- Restricted user-management and Prometheus endpoints
- Public health responses without component details
- Request validation and database constraints

## Secret management

- Never commit `.env`, passwords, JWT signing keys, access tokens, or production connection strings.
- Keep only placeholders in `.env.example` and documentation.
- Generate independent, high-entropy values for database, administrator, and JWT secrets.
- Inject production secrets through the deployment platform's secret manager.
- Rotate a secret immediately if it is exposed. Removing it from a file or Git history does not make
  the exposed value safe again.
- Do not place secrets on command lines in shared environments because process and shell history may
  retain them.

Before committing, review staged changes and run an approved secret scanner if one is available.
GitHub push protection or an equivalent server-side control should also be enabled.

## Candidate data

- Collect only the personal data required to operate the hiring drive.
- Restrict access according to role and venue responsibility.
- Do not log passwords, tokens, candidate contact details, or full request bodies.
- Define retention and deletion rules before using real candidate data.
- Use TLS for every non-local connection and encrypt backups.

## Reporting a vulnerability

Do not open a public issue containing exploit details, secrets, or candidate data. Contact the
repository owner privately so the problem can be assessed and credentials can be rotated before any
public disclosure.

This document describes engineering safeguards, not a claim of regulatory certification. A real
deployment still requires threat modelling, dependency review, access auditing, and applicable
privacy and employment-law review.

