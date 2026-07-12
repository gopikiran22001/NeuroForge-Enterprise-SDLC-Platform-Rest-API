# NeuroForge Enterprise SDLC Platform — Backend

REST API backend for the NeuroForge Enterprise SDLC Platform, built with Spring Boot 3.5 and PostgreSQL.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security + JWT (HttpOnly cookies) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL |
| Connection Pool | HikariCP |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Maven |

---

## Project Structure

```
src/main/java/com/stdace/neuroforge/
├── controller/      # REST controllers (Auth, User, Project, Team, Sprint, Milestone)
├── service/         # Business logic
├── repository/      # Spring Data JPA repositories
├── models/          # JPA entities (User, Project, Team, Sprint, Milestone, RefreshToken)
├── dto/             # Request / Response DTOs
├── security/        # JWT filter, JWT service, security config
├── config/          # CORS, application configuration
├── exception/       # Global exception handler
├── enums/           # Shared enumerations
└── mapper/          # Entity ↔ DTO mappers
```

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL database (local or hosted, e.g. Neon)

### 1. Clone and configure

```bash
git clone <repo-url>
cd Backend
cp .env.example .env
# Edit .env and fill in your values
```

### 2. Configure IntelliJ IDEA (local dev)

Open **Run → Edit Configurations** for your Spring Boot run config and set the **EnvFile** to point to `Backend/.env`, or add each variable manually under **Environment variables**.

> The app reads all configuration from OS environment variables. No `.env` file is loaded at runtime — IntelliJ injects the vars before the JVM starts.

### 3. Run

```bash
./mvnw spring-boot:run
```

The API will be available at `http://localhost:8080`.

Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## Environment Variables

Copy `.env.example` to `.env` and fill in values. **Never commit `.env`.**

| Variable | Description | Example |
|---|---|---|
| `WEBAPP_ORIGIN` | Frontend URL for CORS | `http://localhost:3000` |
| `DB_URL` | PostgreSQL JDBC URL | `jdbc:postgresql://host/db` |
| `DB_USERNAME` | Database username | `postgres` |
| `DB_PASSWORD` | Database password | `secret` |
| `JWT_SECRET` | JWT signing secret (min 32 chars) | `a-strong-random-secret` |
| `JWT_ACCESS_TOKEN_EXPIRATION_SECONDS` | Access token TTL | `900` (15 min) |
| `JWT_REFRESH_TOKEN_EXPIRATION_SECONDS` | Refresh token TTL | `604800` (7 days) |

---

## API Endpoints

### Authentication — `/api/auth`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Register a new user |
| `POST` | `/api/auth/login` | Login and receive JWT cookies |
| `POST` | `/api/auth/logout` | Invalidate session |
| `POST` | `/api/auth/refresh` | Refresh access token |

### Users — `/api/users`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/users/profile` | Get current user's profile |
| `GET` | `/api/users` | Search / list users |
| `POST` | `/api/users` | Create a user |
| `PUT` | `/api/users` | Update current user |
| `PUT` | `/api/users?id={id}` | Update a user by ID (admin) |
| `DELETE` | `/api/users?id={id}` | Delete a user |

### Projects — `/api/projects`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/projects` | Search / list projects |
| `GET` | `/api/projects/{id}` | Get project by ID |
| `POST` | `/api/projects` | Create a project |
| `PUT` | `/api/projects/{id}` | Update a project |
| `DELETE` | `/api/projects/{id}` | Delete a project |

### Teams — `/api/teams`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/teams` | Search / list teams |
| `GET` | `/api/teams/{id}` | Get team by ID |
| `POST` | `/api/teams` | Create a team |
| `PUT` | `/api/teams/{id}` | Update a team |
| `DELETE` | `/api/teams/{id}` | Delete a team |

### Sprints — `/api/sprints`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/sprints` | Search / list sprints |
| `GET` | `/api/sprints/{id}` | Get sprint by ID |
| `POST` | `/api/sprints` | Create a sprint |
| `PUT` | `/api/sprints/{id}` | Update a sprint |
| `DELETE` | `/api/sprints/{id}` | Delete a sprint |

### Milestones — `/api/milestones`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/milestones` | Search / list milestones |
| `GET` | `/api/milestones/{id}` | Get milestone by ID |
| `POST` | `/api/milestones` | Create a milestone |
| `PUT` | `/api/milestones/{id}` | Update a milestone |
| `DELETE` | `/api/milestones/{id}` | Delete a milestone |

---

## Docker

### Build image

```bash
docker build -t neuroforge-backend:latest .
```

### Run container

```bash
docker run -p 8080:8080 \
  -e DB_URL=jdbc:postgresql://your-host/your-db \
  -e DB_USERNAME=your_user \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_strong_secret_min_32_chars \
  -e JWT_ACCESS_TOKEN_EXPIRATION_SECONDS=900 \
  -e JWT_REFRESH_TOKEN_EXPIRATION_SECONDS=604800 \
  -e WEBAPP_ORIGIN=https://yourdomain.com \
  neuroforge-backend:latest
```

All configuration is supplied via environment variables — no `.env` file is needed inside the container.

---

## Health Check

Spring Actuator endpoints are exposed:

```
GET /actuator/health   →  Liveness probe
GET /actuator/info     →  App info
```

---

## Notes

- JWT tokens are stored in **HttpOnly, SameSite=Lax** cookies — not accessible via JavaScript (XSS protection).
- CORS is restricted to the `WEBAPP_ORIGIN` env var.
- `spring.jpa.hibernate.ddl-auto=update` — schema is auto-managed by Hibernate. Switch to `validate` in production with a proper migration tool (Flyway/Liquibase).
