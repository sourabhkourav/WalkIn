# Operations

## Health and metrics

The following health endpoints are public and do not expose component details:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`

Prometheus metrics are available at `/actuator/prometheus` only to an authenticated administrator.

## Runtime behaviour

The production profile disables SQL output, uses bounded database-pool settings, enables graceful
shutdown, honours trusted proxy forwarding headers, and emits ECS-formatted structured logs. Docker
Compose uses the readiness endpoint for its application health check.

## Deployment responsibilities

The deployment platform must provide:

- HTTPS termination and trusted proxy configuration
- Secret-manager integration and credential rotation
- Network restrictions for the API, database, and metrics endpoint
- Automated PostgreSQL backups with tested restores
- Central log collection with personal-data redaction and access controls
- Metrics, alerting, capacity limits, and incident response procedures
- Supported container and dependency update processes

Do not expose PostgreSQL directly to the public internet. Production deployments should pin trusted
images, run with the least privileges available, and separate development credentials and data from
production.

