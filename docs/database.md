# Database and migrations

WalkIn uses PostgreSQL in deployed and Docker environments. Flyway owns schema evolution, while
Hibernate validates entity mappings at application startup.

## Migration rules

- Add schema changes as a new versioned file under `src/main/resources/db/migration`.
- Never edit a migration that has already been applied to a shared or deployed database.
- Do not create placeholder or empty migration files.
- Run the backend tests before applying a new migration locally.
- Review destructive statements and provide a backup and rollback plan before production use.

Migration checksums protect the integrity of the schema history. If a checksum differs, determine
why before repairing anything. Directly editing `flyway_schema_history` is not a normal deployment
workflow and can hide a real schema mismatch.

## Inspect local data

Use the PostgreSQL client inside the Docker container:

```powershell
docker compose exec postgres psql -U walkin -d walkin_db
```

Useful `psql` commands include `\dt` to list tables, `\d <table>` to describe a table, and `\q` to
exit. Avoid copying candidate data into public issues or logs; use synthetic records for examples.

## Data ownership

PostgreSQL data is stored in the Docker volume named `postgres-data`. Production environments need
encrypted backups, restricted database access, a retention policy, and regularly tested restores.

Hiring drives store only a SHA-256 hash of their public registration token. Database exports and
backups therefore do not directly contain a usable public registration link. The raw token is shown
only in the drive-creation response and must not be written to application logs.

The `hiring_drive_round` table connects a hiring drive to reusable `company_custom_round` records.
Its unique constraints protect both the round assignment and its order within a drive. A positive
order check prevents invalid queue positions even when data is written outside the application.

Registration-field requirements are stored on `hiring_drive` because every drive has exactly one
standard form configuration. Database checks restrict each value to `HIDDEN`, `OPTIONAL`, or
`REQUIRED`. Existing and new drives default to required identity/contact fields and a hidden resume,
so applying the migration does not introduce null configuration values.

The `candidate_registration` table stores one venue registration independently of the legacy
student table. A random UUID is the external reference; the sequential primary key remains internal.
Unique constraints prevent duplicate non-null email addresses or contact numbers within the same
drive. Check constraints protect notification channels, advance-notice limits, and registration
statuses even when records are written outside the API.

V10 adds queue audit fields (`status_changed_at`, `status_changed_by`) and an optimistic-lock
`version`. Existing rows use their registration time as the initial status-change time. JPA manages
the version so concurrent venue updates cannot silently overwrite one another.
