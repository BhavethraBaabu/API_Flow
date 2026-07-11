# APIFlow

**Real-time API reliability monitoring platform** — built as an event-drive

microservices system that ingests live API traffic, detects latency spikes
and failures, auto-creates incidents, and surfaces everything on a live
dashboard.

Think "Datadog/PagerDuty, scoped down" — built from scratch to learn and
demonstrate the architecture patterns those tools are built on.

---

## Why this project exists

Most portfolio projects are CRUD apps. APIFlow is deliberately not one — it's
an exercise in the same problems backend/platform teams solve in production:

- How do you ingest a high-throughput stream of events without blocking the
  services that produce them? → **Kafka**
- How do you keep services independently deployable and language-agnostic? →
  **microservices behind a single gateway**
- How do you secure service-to-service and user-to-service calls without a
  shared session store? → **stateless JWT auth**
- How do you turn "the API is slow" into "here's an incident, here's who's
  affected, here's the timeline"? → **event-driven incident detection**

## Architecture

```
                        ┌──────────────────┐
                        │   Next.js UI      │  (Vercel)
                        └────────┬──────────┘
                                 │
                        ┌────────▼──────────┐
                        │  gateway-service   │  routing · CORS · request logging
                        └────────┬──────────┘
              ┌──────────────────┼───────────────────┐
      ┌───────▼───────┐  ┌───────▼────────┐   ┌───────▼────────┐
      │ authentication │  │ event-collector│   │  monitoring    │  ...
      │   -service     │  │   -service     │   │   -service     │
      └───────┬────────┘  └───────┬────────┘   └───────┬────────┘
              │                   │  Kafka             │
         ┌────▼────┐        ┌─────▼─────┐         ┌─────▼─────┐
         │ Postgres│        │  Kafka    │         │ Postgres  │
         └─────────┘        └───────────┘         └───────────┘
```

Each service owns its own data, communicates over REST (sync) and Kafka
(async), and is independently containerized.

## Services

| Service | Status | Responsibility |
|---|---|---|
| **`authentication-service`** | ✅ Live | Registration, login, JWT issuance + refresh, BCrypt hashing, RBAC (Admin/Engineer/Manager) |
| **`gateway-service`** | ✅ Live | Single entry point, request routing, CORS, request/latency logging |
| `event-collector-service` | 🔜 Next | Publishes incoming API call events to Kafka |
| `monitoring-service` | Planned | Latency/availability aggregation, P50/P95/P99 |
| `incident-service` | Planned | Automatic incident detection + lifecycle management |
| `notification-service` | Planned | Email / Slack / webhook alerting |
| `analytics-service` | Planned | Historical trends and reporting |
| `audit-service` | Planned | Immutable audit trail of system + user actions |
| `frontend` | Planned | Next.js live dashboard (deployed to Vercel) |

## Tech stack

**Backend:** Java 21, Spring Boot 3.3, Spring Security, Spring Cloud Gateway,
Spring Data JPA, PostgreSQL, Kafka *(upcoming)*
**Frontend:** Next.js, React *(upcoming)*
**Infra:** Docker, Docker Compose, multi-stage builds
**API docs:** OpenAPI / Swagger UI (auto-generated)

## What's implemented so far — `authentication-service`

- Email + password registration with BCrypt password hashing
- Stateless JWT authentication — short-lived (15 min) access tokens, longer-
  lived (7 day) refresh tokens
- Role-based authorization (`ADMIN`, `ENGINEER`, `MANAGER`) baked into the
  token's granted authorities
- Centralized exception handling → consistent JSON error responses instead
  of leaking stack traces
- Bean validation on every request (`@NotBlank`, `@Email`, `@Size`)
- Integration tests (`AuthControllerIT`) covering register → login and
  invalid-credential rejection, run against an in-memory H2 database
- Swagger UI for interactive API exploration
- 12-factor config — every secret/URL is environment-variable overridable,
  with sane local defaults

## Run it locally

```bash
git clone https://github.com/BhavethraBaabu/API_Flow.git
cd API_Flow
cp .env.example .env
cd docker
docker compose up --build
```

| Service | URL |
|---|---|
| Gateway (entry point) | http://localhost:8080 |
| Auth service (direct) | http://localhost:8081 |
| Swagger UI | http://localhost:8081/swagger-ui.html |

### Try the auth flow

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Ada Lovelace","email":"ada@apiflow.dev","password":"SuperSecret123"}'

curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@apiflow.dev","password":"SuperSecret123"}'
```

### Run tests

```bash
cd authentication-service
mvn test
```

## Deployment

- **Frontend** → Vercel (Next.js), once built.
- **Backend services** → containerized and deployed to a Docker-friendly
  host (Render / Railway / Fly.io). Vercel's serverless runtime doesn't run
  long-lived JVM, Kafka, or Postgres workloads, so the split is deliberate:
  static/edge-friendly frontend on Vercel, stateful services elsewhere,
  fronted by `gateway-service`.

## Roadmap

- [x] Auth service — JWT, RBAC, Postgres
- [x] API Gateway — routing, CORS, request logging
- [ ] Kafka + `event-collector-service`
- [ ] `monitoring-service` — latency/availability computation
- [ ] `incident-service` — automatic detection + lifecycle
- [ ] `notification-service` — email/Slack/webhook alerts
- [ ] `analytics-service` + `audit-service`
- [ ] Next.js dashboard, deployed to Vercel
- [ ] Observability: Prometheus + Grafana + OpenTelemetry tracing
- [ ] Gateway rate limiting (Redis-backed)

## Author

**Bhavethra Baabu** — Master's in Computer Science, Clark University
(graduating August 2026)
[GitHub](https://github.com/BhavethraBaabu) ·
[LinkedIn](https://linkedin.com/in/bhavethrab24)
