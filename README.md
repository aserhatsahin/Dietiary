
# Dietary – Dietitian–Client Management Platform

Dietary is a SaaS platform that digitizes the dietitian–client workflow. Dietitians manage clients, track body measurements, set goals, and create flexible meal plans through a web panel. Clients follow their daily nutrition plans via a mobile application with meal selection, tracking, progress visualization, and notifications.

The current scope targets a single dietitian account with multiple clients. The backend architecture remains multi-tenant ready by scoping all client data under a dietitian context.

## Overview

Dietary covers the full nutrition workflow:

- Dietitian side: client management, measurement tracking, goal calculation, flexible meal plan creation, reporting
- Client side: daily meal selection, meal completion tracking, water tracking, progress monitoring, notifications

The system is designed around real-world dietitian practices where meal plans provide multiple alternatives per meal instead of fixed day-based menus.

## Tech Stack

### Backend
- Java 17+
- Spring Boot 3.x
- Spring Security (JWT access & refresh tokens)
- Spring Data JPA (Hibernate)
- PostgreSQL
- Gradle
- OpenAPI / Swagger

### Web Frontend (Dietitian Panel)
- React 18 + TypeScript
- Vite
- TailwindCSS
- React Query (server state management)
- Zustand (global client state)
- Recharts (charts and analytics)

### Mobile App (Client App)
- React Native + Expo
- React Navigation
- Axios
- AsyncStorage (offline cache)
- Expo Notifications


## Project Structure


dietary/
├── backend/          # Spring Boot API
├── web/              # React web panel for dietitians
└── mobile/           # React Native app for clients

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
│   ├── parser/          # PDF parsing (Tarti.com template)
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
│   ├── calculator/      # calorie and macro calculations
│   └── repository/
├── tracking/
│   ├── controller/
│   ├── service/
│   ├── domain/
│   └── repository/
└── common/
├── security/
├── exception/
├── dto/
├── mapper/
└── util/


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
  - PDF upload and parsing (Tarti.com output for demo)
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


## API Endpoints (Draft)

### Authentication
- POST /api/auth/register
- POST /api/auth/login
- POST /api/auth/refresh

### Clients
- GET /api/clients
- POST /api/clients
- GET /api/clients/{id}
- PUT /api/clients/{id}
- DELETE /api/clients/{id}

### Measurements
- GET /api/clients/{id}/measurements
- POST /api/clients/{id}/measurements
- POST /api/clients/{id}/measurements/parse

### Goals
- POST /api/clients/{id}/goals
- GET /api/clients/{id}/goals/current

### Foods
- GET /api/foods/search
- POST /api/foods

### Meal Plans
- POST /api/meal-plans
- PUT /api/meal-plans/{id}
- POST /api/meal-plans/{id}/assign
- GET /api/meal-plans/client/{clientId}/active

### Mobile
- GET /api/mobile/daily-plan
- POST /api/mobile/select-meal-option
- POST /api/mobile/track-meal
- POST /api/mobile/track-water
- GET /api/mobile/progress

## Performance Targets

- Page load time < 2 seconds
- Typical API response < 500 ms
- Mobile app launch < 3 seconds

## Development Setup

Requirements:
- Java 17+
- Node.js 20+
- PostgreSQL 15+
- Git

Backend:
```bash
cd backend
./gradlew bootRun
````

Web:

```bash
cd web
npm install
npm run dev
```

Mobile:

```bash
cd mobile
npm install
npx expo start
```

---

## Out of Scope (MVP)

* Messaging system
* Appointment scheduling
* Payment integration
* Exercise planning
* Photo-based progress tracking
* Barcode scanning
* AI-driven meal suggestions

