# Dietary Backend

Spring Boot REST API for the Dietary dietitian-client management platform.

## Requirements

- **Java 17+** (required for Spring Boot 4.x)
- **Docker** (runs PostgreSQL via the root `docker-compose.yml`)
- Gradle is not needed; the wrapper (Gradle 9.9) is included

## Setup

### 1. Start PostgreSQL

From the repository root:

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with database `dietiary_dev`, user `dietiary`. These match `src/main/resources/application-dev.yml`.

If port 5432 is already in use (for example by a PostgreSQL installed on your machine), pick another port. The `dev` profile reads the same variable:

```bash
export POSTGRES_PORT=5433
docker compose up -d
```

### 2. Build

```bash
./gradlew build
```

### 3. Run

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The `dev` profile runs on `http://localhost:8081` and seeds a list of common foods.

### Database migrations

[Flyway](https://flywaydb.org/) creates and updates the schema from `src/main/resources/db/migration` on startup. Hibernate only validates it (`ddl-auto: validate`). To change the schema, add a new file such as `V2__add_something.sql`; never edit a migration that has already been applied.

If your local database was created before migrations existed (by Hibernate), or before `docker-compose.yml` mounted the volume at `/var/lib/postgresql` (required by the PostgreSQL 18 image), reset it once:

```bash
docker compose down -v && docker compose up -d
```

## Tests

```bash
./gradlew test
```

- **Unit tests** (`*Test`) run anywhere.
- **Integration tests** (`*IT`) start PostgreSQL 18 in Docker via [Testcontainers](https://testcontainers.com/), apply the Flyway migrations, and call the API over HTTP. They are skipped when Docker is not running.

GitHub Actions runs all of them on every pull request.

## API Documentation

Once the application is running:
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8081/api-docs

## API Endpoints

All endpoints except `/api/auth/**` need an `Authorization: Bearer <token>` header.

### Authentication (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | /api/auth/register | Register a dietitian account |
| POST | /api/auth/login | Login (dietitian or client) |
| POST | /api/auth/refresh | Get new tokens with a refresh token |
| POST | /api/auth/client/accept-invite | Client sets a password using an invite token |

### Dietitian

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/clients | List own clients |
| POST | /api/clients | Create a client |
| GET | /api/clients/{id} | Get a client |
| PUT | /api/clients/{id} | Update a client |
| DELETE | /api/clients/{id} | Delete a client |
| PATCH | /api/clients/{id}/toggle-active | Activate / deactivate a client |
| POST | /api/clients/{id}/invite | Create an invite token for the client (calling again replaces it) |
| GET | /api/clients/{clientId}/measurements | Measurement history |
| GET | /api/clients/{clientId}/measurements/latest | Latest measurement |
| POST | /api/clients/{clientId}/measurements | Add a measurement (BMI and deltas are calculated) |
| PUT | /api/clients/{clientId}/measurements/{measurementId} | Correct a measurement (BMI is recalculated) |
| DELETE | /api/clients/{clientId}/measurements/{measurementId} | Delete a measurement |
| GET | /api/clients/{clientId}/goals | Goal history, newest first |
| GET | /api/clients/{clientId}/goals/current | Current goal |
| POST | /api/clients/{clientId}/goals | Create a goal (BMR, TDEE, calorie target) |
| GET | /api/clients/{clientId}/meal-plans | All of a client's meal plans |
| GET | /api/clients/{clientId}/meal-plans/active | Client's active meal plan |
| GET | /api/clients/{clientId}/progress | Client progress summary |
| GET | /api/foods/search | Search foods |
| GET | /api/foods/categories | Food categories |
| POST | /api/foods | Add a custom food |
| GET | /api/meal-plans/templates | List meal plan templates |
| GET | /api/meal-plans/{id} | Get a meal plan |
| POST | /api/meal-plans | Create a meal plan |
| PUT | /api/meal-plans/{id} | Update a meal plan |
| POST | /api/meal-plans/{id}/activate | Make a plan the client's active plan |
| DELETE | /api/meal-plans/{id} | Delete a plan or template (409 if it has tracking history) |

### Client (mobile)

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/mobile/me | My profile |
| GET | /api/mobile/measurements | My measurements (newest first) |
| GET | /api/mobile/measurements/latest | My latest measurement |
| GET | /api/mobile/goal | My current goal |
| GET | /api/mobile/progress | My progress (`fromDate`/`toDate` optional, default last 30 days) |
| GET | /api/mobile/tracking/daily | Today's plan with tracking state |
| POST | /api/mobile/tracking/meals | Log the alternative eaten for a meal |
| POST | /api/mobile/tracking/water | Log water intake |

## Project Structure

```
backend/src/main/java/com/dietary/
├── DietaryApplication.java   # Main entry point
├── auth/                     # Register, login, refresh tokens, client invites
├── client/                   # Client profiles
├── measurement/              # Body measurements, BMI
├── goal/                     # Goals, BMR / TDEE / calorie target
├── food/                     # Food database + seed data
├── mealplan/                 # Alternative-based meal plans
├── tracking/                 # Daily meal and water tracking, progress
└── common/                   # Security (JWT), config, DTOs, exception handling
```

Each module follows `controller/` (+ `dto/`), `service/`, `domain/`, `repository/`.

## Configuration

The base configuration (`application.yml`) is the production one. It reads everything environment-specific from environment variables. The `dev` and `test` profiles add local values on top.

| Variable | Required | Default | What it is |
|---|---|---|---|
| `SPRING_DATASOURCE_URL` | yes | none | JDBC URL, e.g. `jdbc:postgresql://host:5432/dietiary` |
| `SPRING_DATASOURCE_USERNAME` | yes | none | Database user |
| `SPRING_DATASOURCE_PASSWORD` | yes | none | Database password |
| `JWT_SECRET` | yes | none | Token signing key, at least 32 bytes: `openssl rand -base64 48`. The app refuses to start without it. |
| `CORS_ALLOWED_ORIGINS` | no | `http://localhost:5173,http://localhost:3000` | Comma-separated origins of the web panel |
| `PORT` | no | `8080` | HTTP port (many hosting platforms set this) |
| `SPRINGDOC_API_DOCS_ENABLED`, `SPRINGDOC_SWAGGER_UI_ENABLED` | no | `true` | Set to `false` to hide the API docs |

Health checks: `GET /actuator/health`, plus `/actuator/health/liveness` and `/actuator/health/readiness`. No other Actuator endpoints are exposed.

## Docker

`backend/Dockerfile` is a multi-stage build. The final image has only a JRE and the app's layers, and it runs as a non-root user.

Run the image locally together with PostgreSQL, from the repository root:

```bash
docker compose --profile app up -d --build
curl http://localhost:8080/actuator/health
```

CI builds the same image on every PR, starts it with PostgreSQL, waits for the health check, and then makes a real API call.

## Authentication Flow

### Dietitian Registration & Login
1. Dietitian registers via `POST /api/auth/register`
2. Dietitian logs in via `POST /api/auth/login`
3. Use access token in `Authorization: Bearer <token>` header

### Client Onboarding
1. Dietitian creates a client profile via `POST /api/clients`
2. Dietitian creates an invite via `POST /api/clients/{id}/invite` and shares the returned `inviteToken` with the client (valid for 7 days; email delivery is not built yet)
3. Client accepts invite via `POST /api/auth/client/accept-invite`
4. Client logs in normally via `POST /api/auth/login`

### Token Refresh
1. When access token expires, call `POST /api/auth/refresh` with refresh token
2. Receive new access + refresh tokens
