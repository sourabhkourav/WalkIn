# Security

Security is part of every WalkIn change because the system processes candidate contact information
and recruitment decisions.

## Current controls

- Stateless JWT authentication with a configured issuer and short token lifetime
- Three-level role authorization for platform administrators, company administrators, and recruiters
- Company ownership embedded in signed JWTs and checked against every company resource
- Tenant-filtered collection queries and `403 Forbidden` responses for cross-company access
- BCrypt-compatible delegated password hashing
- Environment-based database, bootstrap-user, and signing-key configuration
- No stack traces or internal exception messages in API responses
- Restricted user-management and Prometheus endpoints
- Public health responses without component details
- Request validation and database constraints
- One-time public registration tokens stored as SHA-256 hashes rather than reusable raw values
- Minimal public drive responses that omit internal IDs, contact details, and token metadata
- Public registration acknowledgements that never echo candidate details or resume content
- PDF resume validation with 2 MB file and 3 MB request limits
- Candidate registration links contain only a drive token and never candidate information
- Platform administrators remain company-less; company administrators and recruiters must belong
  to exactly one company

## Tenant boundary

The JWT `companyId` claim is an authorization input, not a frontend preference. Company-scoped
controllers compare it with the resource owner's company before reading candidate data, resumes,
drive rounds, or changing queue state. Collection queries are filtered at the repository level so
other tenants' rows are not loaded and later discarded.

Only `PLATFORM_ADMIN` may manage all companies and application users. `COMPANY_ADMIN` may configure
its own company and drives. `RECRUITER` may read its company workspace and perform permitted venue
queue actions. Legacy APIs without a hiring-drive ownership relationship remain platform-only.

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
- Reject values submitted for fields the hiring drive configured as hidden.
- Keep candidate-controlled notification destinations separate from company-requested contact data.
- Treat registration links like venue invitations: share them intentionally and close the drive
  when registration should stop.
- Omit notification destinations and resume bytes from candidate queue responses.
- Expose resume content only through the authenticated PDF download endpoint.
- Record the authenticated operator for every real queue-status change.
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
