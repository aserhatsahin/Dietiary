# Dietary Backend

Spring Boot REST API for the Dietary dietitian-client management platform.

## Requirements

- **Java 17+** (required for Spring Boot 3.x)
- **PostgreSQL 15+**
- **Gradle 8.5** (wrapper included)

## Setup

### 1. Install Java 17+

The project requires Java 17 or higher. Download and install from one of:
- [Eclipse Temurin (Adoptium)](https://adoptium.net/temurin/releases/?version=17)
- [Oracle JDK 17](https://www.oracle.com/java/technologies/downloads/#java17)
- [Amazon Corretto 17](https://aws.amazon.com/corretto/)

After installation, set `JAVA_HOME` environment variable or update your `PATH`.

### 2. Set up PostgreSQL Database

Create a database for development:

```sql
CREATE DATABASE dietary_dev;
```

Update the database credentials in `src/main/resources/application-dev.yml` if needed:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/dietary_dev
    username: postgres
    password: postgres
```

### 3. Build the Project

```bash
# Windows
.\gradlew.bat build

# Linux/Mac
./gradlew build
```

### 4. Run the Application

```bash
# Windows
.\gradlew.bat bootRun --args='--spring.profiles.active=dev'

# Linux/Mac
./gradlew bootRun --args='--spring.profiles.active=dev'
```

The application will start on `http://localhost:8080`

## API Documentation

Once the application is running, access Swagger UI at:
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

## API Endpoints

### Authentication

| Method | Endpoint                    | Description                          | Auth Required |
|--------|-----------------------------|--------------------------------------|---------------|
| POST   | /api/auth/register          | Register new dietitian account       | No            |
| POST   | /api/auth/login             | Login (dietitian or client)          | No            |
| POST   | /api/auth/refresh           | Refresh access token                 | No            |
| POST   | /api/auth/client/accept-invite | Client accepts invite             | No            |

## Project Structure

```
backend/src/main/java/com/dietary/
├── DietaryApplication.java          # Main entry point
├── auth/                             # Authentication module
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── dto/
│   ├── domain/                       # Entities
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── RefreshToken.java
│   │   └── ClientInvite.java
│   ├── repository/
│   └── service/
├── client/                           # Client profile module
│   ├── domain/
│   │   ├── Client.java
│   │   └── Gender.java
│   └── repository/
├── measurement/                      # Body measurements
│   └── domain/
├── goal/                             # Client goals
│   └── domain/
├── food/                             # Food database
│   └── domain/
├── mealplan/                         # Meal plans
│   └── domain/
│       ├── MealPlan.java
│       ├── Meal.java
│       ├── MealOption.java
│       └── MealOptionItem.java
├── tracking/                         # Daily tracking
│   └── domain/
│       ├── DailyTracking.java
│       └── WaterTracking.java
└── common/
    ├── config/
    ├── dto/
    ├── exception/
    └── security/
```

## Environment Variables (Production)

For production deployment, set these environment variables:

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
1. Dietitian creates client profile (future endpoint)
2. System sends invite email with token
3. Client accepts invite via `POST /api/auth/client/accept-invite`
4. Client logs in normally via `POST /api/auth/login`

### Token Refresh
1. When access token expires, call `POST /api/auth/refresh` with refresh token
2. Receive new access + refresh tokens
