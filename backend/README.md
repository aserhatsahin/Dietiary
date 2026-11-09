# Dietary Backend

Spring Boot REST API for the Dietary dietitian-client management platform.

## Requirements

- **Java 17+** (required for Spring Boot 3.x)
- **Docker** (runs PostgreSQL via the root `docker-compose.yml`)
- Gradle is not needed; the wrapper (Gradle 8.5) is included

## Setup

### 1. Start PostgreSQL

From the repository root:

```bash
docker compose up -d
```

This starts PostgreSQL on `localhost:5432` with database `dietiary_dev`, user `dietiary`. These match `src/main/resources/application-dev.yml`.

### 2. Build

```bash
./gradlew build
```

### 3. Run

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The `dev` profile runs on `http://localhost:8081` and lets Hibernate create the schema (`ddl-auto: update`). The default profile uses `ddl-auto: validate` and needs an existing schema. Database migrations are on the [roadmap](../docs/ROADMAP.md).

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
| GET | /api/clients/{clientId}/goals/current | Current goal |
| POST | /api/clients/{clientId}/goals | Create a goal (BMR, TDEE, calorie target) |
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

### Client (mobile)

| Method | Endpoint | Description |
|---|---|---|
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

## Environment Variables (Production)

Today only `JWT_SECRET` is read from the environment. Reading the database settings from the environment is on the roadmap. The planned variables are:

```bash
DATABASE_URL=jdbc:postgresql://host:5432/dietary
DATABASE_USERNAME=your_user
DATABASE_PASSWORD=your_password
JWT_SECRET=your-256-bit-secret-key
JWT_ACCESS_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000
```

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
