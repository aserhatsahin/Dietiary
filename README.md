
# Dietary – Dietitian–Client Management Platform

[![Backend CI](https://github.com/aserhatsahin/Dietiary/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/aserhatsahin/Dietiary/actions/workflows/backend-ci.yml)

Dietary is a SaaS platform that digitizes the dietitian–client workflow. Dietitians manage clients, track body measurements, set goals, and create flexible meal plans through a web panel. Clients follow their daily nutrition plans via a mobile application with meal selection, tracking, progress visualization, and notifications.

The current scope targets a single dietitian account with multiple clients. The backend architecture remains multi-tenant ready by scoping all client data under a dietitian context.

## Live demo

- **API docs (Swagger UI):** https://dietiary-api.onrender.com/swagger-ui.html
- **Health:** https://dietiary-api.onrender.com/actuator/health

The API runs on a free Render instance with a Neon PostgreSQL database and deploys from `main` after CI passes (setup: [docs/DEPLOY.md](docs/DEPLOY.md)). If it has been asleep, the first request can take a few minutes.

To try it in Swagger UI: call `POST /api/auth/login` with a demo account below, copy `accessToken` from the response, click **Authorize** and paste it.

Demo accounts (shared and public, so other visitors may change the data):

| Role | Email | Password |
|---|---|---|
| Dietitian | `demo@dietiary.app` | `Dietiary-demo-2026` |
| Client (mobile endpoints) | `elif.demo@dietiary.app` | `Dietiary-demo-2026` |

The dietitian has three clients with weeks of measurements and goals, a meal plan with alternatives, and a template. Elif has an account and four weeks of meal and water tracking.

## Status

Work in progress. The **backend API** is implemented and runnable. The **web panel** and **mobile app** have not been started yet. The [roadmap](docs/ROADMAP.md) lists what works, what is missing, and what comes next.

## Overview

Dietary covers the full nutrition workflow:

- Dietitian side: client management, measurement tracking, goal calculation, flexible meal plan creation, reporting
- Client side: daily meal selection, meal completion tracking, water tracking, progress monitoring, notifications

The system is designed around real-world dietitian practices where meal plans provide multiple alternatives per meal instead of fixed day-based menus.

## Tech Stack

### Backend
- Java 17+
- Spring Boot 4.1
- Spring Security (JWT access & refresh tokens)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Gradle
- OpenAPI / Swagger

### Web Frontend (Dietitian Panel) (planned)
- React 18 + TypeScript
- Vite
- TailwindCSS
- TanStack Query (server state management)
- Recharts (charts and analytics)

### Mobile App (Client App) (planned)
- Kotlin Multiplatform + Compose Multiplatform (Android and iOS)
- Ktor client + kotlinx.serialization
- Koin (dependency injection)
- Local cache for offline viewing


## Project Structure


dietary/
├── backend/          # Spring Boot API
├── web/              # React web panel for dietitians (planned)
└── mobile/           # Kotlin Multiplatform app for clients (planned)

## Backend Architecture

The backend follows a pragmatic clean architecture approach.  
It maintains a familiar layered API structure while isolating domain logic from infrastructure concerns.

### Conceptual Layers

- API (Controllers): HTTP handling, DTOs, validation
- Application (Services): orchestration, transactions, authorization
- Domain: business rules and calculations
- Infrastructure: persistence, file handling, external integrations

### Package-by-Feature Structure


backend/src/main/java/com/dietary
├── auth/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── client/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── measurement/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── goal/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── food/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── mealplan/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
├── tracking/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
└── common/
├── config/
├── security/
├── exception/
└── dto/


## Core Domain Concepts

### Meal Plan Model (Alternative-Based)

Meal plans are not tied to calendar days.

A plan consists of meals, and each meal contains multiple selectable alternatives.  
Clients choose one alternative per meal each day.

Conceptual structure:
Meal Plan
├── Meal (Breakfast)
│    ├── Option A
│    ├── Option B
│    └── Option C
├── Meal (Lunch)
│    ├── Option A
│    ├── Option B
│    └── Option C
└── Meal (Dinner)
├── Option A
├── Option B
└── Option C

This model reflects real dietitian programs that offer flexible combinations rather than fixed daily menus.

## Features

### Dietitian Web Panel
- Authentication and profile management
- Client management (add, edit, list, soft delete)
- Measurement tracking
  - Manual entry
  - PDF upload and parsing (Tarti.com output for demo) (planned)
  - Historical measurement tables and charts
- Goal setting
  - BMR and TDEE calculation
  - Target calorie calculation
  - Weekly expected weight change
- Meal plan creation
  - Alternative-based meal structure
  - Macro-based planning
  - Calorie/percentage-based planning
- Plan assignment (single active plan per client)
- Progress reports and analytics dashboard

### Client Mobile App
- Authentication
- Daily plan view
- Meal alternative selection per meal
- Meal completion tracking
- Water intake tracking
- Progress visualization (weight and measurements)
- Push notifications
- Offline viewing of recent plans and measurements

## Multi-Tenancy Model

- Demo assumes a single dietitian account
- Data model remains multi-tenant ready
- All client-related entities are scoped by `dietitian_id`
- Authorization rules enforce strict data isolation

## Authentication & Security

- JWT-based authentication (access and refresh tokens)
- Role-based access control (Dietitian / Client)
- BCrypt password hashing
- HTTPS required for production
- Authorization rules:
  - Dietitians can only access their own clients
  - Clients can only access their own data

## Automated Calculations

- BMR (Harris–Benedict formula)
- TDEE (activity factor based)
- Daily calorie target
- Weekly expected weight change
- BMI calculation on measurement entry

## Database Schema (High Level)

Main entities:
- Users (Dietitians)
- Clients
- Measurements
- Goals
- Foods
- MealPlans
- Meals
- MealOptions
- MealOptionItems
- DailyTracking
- WaterTracking


## API Endpoints

The full, up-to-date endpoint list is in [backend/README.md](backend/README.md#api-endpoints). With the app running, Swagger UI is at `http://localhost:8081/swagger-ui.html`.

## Development Setup

Requirements:
- Java 17+
- Docker (for PostgreSQL)

```bash
# Start PostgreSQL
docker compose up -d

# Run the backend with the dev profile (http://localhost:8081)
cd backend
./gradlew bootRun --args='--spring.profiles.active=dev'
```

Or run the backend's Docker image together with PostgreSQL (http://localhost:8080):

```bash
docker compose --profile app up -d --build
```

If port 5432 is already in use on your machine, set `POSTGRES_PORT=5433` first. Production configuration (environment variables) is described in [backend/README.md](backend/README.md#configuration).

Setup steps for the web panel and mobile app will be added once those apps exist.

---

## Out of Scope (MVP)

* Messaging system
* Appointment scheduling
* Payment integration
* Exercise planning
* Photo-based progress tracking
* Barcode scanning
* AI-driven meal suggestions

