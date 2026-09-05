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
