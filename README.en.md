# desafio-hall

[Português](README.md) | [English](README.en.md)

Full-stack application with Angular frontend, Node.js API Gateway, and Spring Boot backend for application, release, approval, and audit management.

## Cross-Cutting Requirements

- Token-based authentication (mock-friendly), with roles: admin / approver / viewer
- Standardized JSON errors: { code, message, details }
- Structured logs with requestId/correlationId propagated from Gateway to backend
- Simple metrics (route counters and response timing at Gateway level)
- Swagger/OpenAPI on Gateway (mandatory) and backend (recommended)

## Executive Summary

This repository contains the solution with clear separation between UI, API gateway, and business services.

Main goals:

- provide REST APIs for applications and releases
- integrate frontend, gateway, and backend with stable contracts
- ensure quality through tests and build standards

Main modules:

- `frontend-angular`: Angular SPA
- `gateway-node`: API Gateway / proxy (`/api/*`)
- `service-backend`: Spring Boot REST API
- `postgres`: PostgreSQL database

## Stack

- Java 17
- Spring Boot 3.5.14
- Node.js
- Angular 21
- Docker Compose

## Quick Start

```bash
cd service-backend
mvn clean install
```

```bash
cd ..
docker compose up -d postgres
```

```bash
cd service-backend
mvn spring-boot:run
```

```bash
cd ../gateway-node
npm install
JWT_SECRET=changeit-changeit-changeit-changeit BACKEND_BASE_URL=http://localhost:8081 npm run dev
```

```bash
cd ../frontend-angular
npm install
npm start
```

Expected endpoints:

- backend: `http://localhost:8081`
- gateway: `http://localhost:3000`
- frontend: `http://localhost:4200`

OpenAPI endpoints:

- Gateway Swagger UI: `http://localhost:3000/swagger-ui`
- Gateway OpenAPI JSON: `http://localhost:3000/api-docs-json`
- Backend Swagger UI: `http://localhost:8081/swagger-ui.html`
- Backend OpenAPI JSON: `http://localhost:8081/api-docs`

Base Flyway migrations in `service-backend/src/main/resources/db/migration` are split by table:

- `V1_1_0__create_application_table.sql`: creates `APPLICATION`
- `V1_1_0_1__create_release_table.sql`: creates `RELEASE`
- `V1_1_0_2__create_approval_table.sql`: creates `APPROVAL`
- `V1_1_0_3__create_audit_log_table.sql`: creates `AUDITLOG`

The main backend entities (`APPLICATION`, `RELEASE`, `APPROVAL`, `AUDITLOG`, and `IDEMPOTENCY_RECORD`) now use `UUID` primary keys. Public filters and path parameters such as `applicationId`, `releaseId`, and `{id}` follow the same format.

Public API base URL:

- `http://localhost:3000/api`

Gateway operational endpoints:

- `GET /healthcheck`
- `GET /metrics`
- `GET /api-docs-json`
- `GET /swagger-ui`
- `PATCH /api/releases/{id}/evidence-url`
- `GET /api/releases/{id}/evidence-score`

Promote endpoint notes:

- supports `Idempotency-Key` header for replay deduplication
- replay with the same completed key returns the same effect without duplicating audit
- in-progress key or concurrent conflict returns `409`

## Policy-as-Code

The backend loads policy JSON at runtime with this precedence order:

1. path from `POLICY_FILE_PATH`
2. `../policy.json` (repository root in local execution)
3. classpath `policy.json` (`service-backend/src/main/resources/policy.json`)

Policy fields enforced by the backend:

- `minApprovals`: minimum number of `APPROVED` approvals required to approve a release
- `minScore`: minimum score required, computed as percentage of `APPROVED` approvals over total approvals for the release
- `freezeWindows`: blocks approval/promotion by environment and time window

## Access Matrix (Roles x Endpoints)

Effective permissions considering Gateway + Backend:

| Endpoint | Method | admin | approver | viewer |
| --- | --- | --- | --- | --- |
| `/api/auth/login` | `POST` | Yes (public) | Yes (public) | Yes (public) |
| `/healthcheck` | `GET` | Yes (public) | Yes (public) | Yes (public) |
| `/metrics` | `GET` | Yes (public) | Yes (public) | Yes (public) |
| `/api-docs-json` | `GET` | Yes (public) | Yes (public) | Yes (public) |
| `/swagger-ui` | `GET` | Yes (public) | Yes (public) | Yes (public) |
| `/api/applications` | `GET` | Yes | Yes | Yes |
| `/api/applications` | `POST` | Yes | No | No |
| `/api/applications/{id}` | `PUT/PATCH` | Yes | No | No |
| `/api/releases` | `GET` | Yes | Yes | Yes |
| `/api/releases` | `POST` | Yes | No | No |
| `/api/releases/{id}/approve` | `POST` | Yes | Yes | No |
| `/api/releases/{id}/disapprove` | `POST` | Yes | Yes | No |
| `/api/releases/{id}/promote` | `POST` | Yes | No | No |
| `/api/releases/{id}/evidence-url` | `PATCH` | Yes | No | No |
| `/api/releases/{id}/evidence-score` | `GET` | Yes | Yes | Yes |
| `/api/approvals` and filters | `GET` | Yes | Yes | No |
| `/api/audit` and filters | `GET/POST` | Yes | No | No |
| `/api/users` | `GET/POST/PUT/DELETE` | Yes | No | No |

Audit notes:

- audited release actions: `CREATE`, `APPROVE`, `DISAPPROVE`, `PROMOTE`, `CHANGE_EVIDENCE_URL`
- `GET /api/audit` supports optional root query filters: `ator`, `acao`, `entidade`, `dataInicio`, `dataFim`

## Docker

```bash
docker compose up --build
```

Important variables are documented in `.env.example`.

## Quality

Backend:

```bash
cd service-backend
mvn clean test
mvn verify
```

Gateway:

```bash
cd gateway-node
npm run lint
npm test
```

Gateway responsibilities currently implemented:

- Authentication and route authorization by role (admin / approver / viewer)
- Input validation for content-type, body format, and required query params
- Structured logging with `x-request-id` and `x-correlation-id`
- Metrics endpoint with request counters by normalized route
- Error normalization with `{ code, message, details }`

Frontend:

```bash
cd frontend-angular
npm run lint
npm run build
npm run test
```

### Policy-as-Code Tests

Main rule coverage:

- `minApprovals` (blocks when valid approvals are below minimum)
- `minScore` (blocks when percentage score is below minimum)
- `freezeWindows` by environment (blocks when active window is in effect)
- policy enforcement in `approveRelease` and `promoteRelease` flows

Test classes:

- `service-backend/src/test/java/com/example/backend/policy/PolicyServiceTest.java`
- `service-backend/src/test/java/com/example/backend/service/ReleaseServiceImplTest.java`

Focused run:

```bash
cd service-backend
mvn -Dtest=PolicyServiceTest,ReleaseServiceImplTest test
```

## Evidence Scoring

Deterministic evidence score endpoint:

- `GET /api/releases/{id}/evidence-score`

Scoring rules (0..100):

- Valid evidence URL (`http/https` with host): `+40`
- URL contains report pattern (`report`, `evidence`, `quality`, `test`, `coverage`): `+20`
- URL contains success indicator (`PASS`, `passed`, `result=pass`, `status=pass`): `+20`
- Release status weight:
	- `DEPLOYED`, `APPROVED_PROD`, `APPROVED_PREPROD`: `+10`
	- `PENDING_PROD`, `PENDING_PREPROD`: `+5`
	- other statuses: `+0`
- Environment weight:
	- `PROD`: `+10`
	- `PREPROD`: `+7`
	- `DEV`: `+5`

Final score is normalized and capped to the `0..100` range.