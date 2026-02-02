# Dietary - Dietitian-Client Management Platform
Dietary is a multi-tenant SaaS platform that digitizes the dietitian-client workflow. Dietitians manage clients, track measurements, and create personalized meal plans through a web panel, while clients follow their daily nutrition plans via a mobile app with progress tracking and notifications.
A comprehensive SaaS platform connecting dietitians with clients for meal planning, progress tracking, and daily nutrition management.

## Overview

Dietary digitizes the dietitian-client workflow. Dietitians manage clients, track measurements, and create personalized meal plans through a web panel, while clients follow their daily nutrition plans via a mobile app.

## Tech Stack

### Backend
- Spring Boot 3.x (Java 17+)
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL
- Gradle

### Web Frontend (Dietitian Panel)
- React 18 + TypeScript
- Vite
- TailwindCSS
- React Query (API state management)
- Zustand (global state)
- Recharts (charts)

### Mobile App (Client App)
- React Native + Expo
- React Navigation
- Axios
- AsyncStorage
- Expo Notifications

## Project Structure
```
dietary/
├── backend/          # Spring Boot API
├── web/             # React web panel for dietitians
└── mobile/          # React Native app for clients
```

## Features

### Dietitian Web Panel
- Client management (add, edit, track)
- Measurement tracking (manual entry + PDF upload)
- Goal setting with automatic calorie calculations
- Meal plan creation (two modes: macro-based and calorie-percentage-based)
- Progress reports and charts
- Dashboard with analytics

### Client Mobile App
- Daily meal plan viewing
- Meal completion tracking
- Water intake tracking
- Progress visualization (weight chart, measurements)
- Push notifications (meal reminders, motivation)
- Offline support

## Key Requirements

### Authentication & Security
- JWT-based authentication
- Role-based access control (Dietitian/Client)
- BCrypt password hashing
- HTTPS required

### Database Schema
Main entities: Users, Clients, Measurements, Goals, Foods, MealPlans, MealPlanItems, DailyTracking, WaterTracking

### Automated Calculations
- BMR (Basal Metabolic Rate) - Harris-Benedict formula
- TDEE (Total Daily Energy Expenditure)
- Target calories based on goals
- Weekly expected weight change

### Meal Planning Modes

**Mode A: Macro-Based Planning**
- Add foods by selecting and specifying portions/grams
- Automatic calorie and macro calculations
- Meal-by-meal planning

**Mode B: Calorie/Percentage-Based Planning**
- Set daily calorie target
- Define macro distribution (Carbs/Protein/Fat %)
- Distribute to meals automatically or manually
- Track remaining calories/macros

### Features NOT in MVP
- Messaging system
- Appointment management
- Payment integration
- Exercise planning
- Photo progress tracking
- Barcode scanner
- AI meal suggestions

## Development Setup

**Requirements:**
- Java 17+
- Node.js 20+
- PostgreSQL 15+
- Git

**Backend:**
```bash
cd backend
./gradlew bootRun
```

**Web:**
```bash
cd web
npm install
npm run dev
```

**Mobile:**
```bash
cd mobile
npm install
npx expo start
```

## API Endpoints

### Authentication
- POST /api/auth/register
- POST /api/auth/login

### Clients
- GET /api/clients
- POST /api/clients
- GET /api/clients/{id}
- PUT /api/clients/{id}
- DELETE /api/clients/{id}

### Measurements
- GET /api/clients/{id}/measurements
- POST /api/clients/{id}/measurements

### Goals
- POST /api/goals
- GET /api/goals/{clientId}

### Foods
- GET /api/foods/search?query=
- POST /api/foods (custom food)

### Meal Plans
- POST /api/meal-plans
- GET /api/meal-plans/client/{clientId}/active
- GET /api/meal-plans/templates

### Mobile
- GET /api/mobile/daily-plan
- POST /api/mobile/track-meal
- POST /api/mobile/track-water
- GET /api/mobile/progress

## Performance Requirements
- Page load: < 2 seconds
- API response: < 500ms
- Mobile app launch: < 3 seconds

## Browser Support
- Chrome (last 2 versions)
- Firefox (last 2 versions)
- Safari (last 2 versions)
- Edge (last 2 versions)

## Mobile Support
- iOS 13+
- Android 8.0+ (API 26)

## License
TBD

## Contact
TBD
