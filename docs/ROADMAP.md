# Dietiary Roadmap

This file tracks where the project stands and what comes next.

## Decisions

- **Backend:** keep building on the existing Spring Boot 3.2 / Java 17 / PostgreSQL code. We are not rewriting it.
- **Dietitian web panel:** React 18 + TypeScript, Vite, TailwindCSS, TanStack Query and Recharts.
- **Client mobile app:** Kotlin Multiplatform with Compose Multiplatform, targeting Android and iOS from one codebase.

The plan puts something people can try online first: a deployed API and web panel. The mobile app comes after that.

## Current state (backend)

| Module | Working endpoints | Missing |
|---|---|---|
| auth | register, login, refresh, accept-invite | nothing creates invites yet, so clients cannot sign up |
| client | list, get, create, update, delete, toggle active | none |
| measurement | list, latest, create (BMI + deltas) | update/delete; PDF import (only the enum exists) |
| goal | current goal, create (BMR/TDEE/calorie target) | goal history |
| food | search, categories, create, seed data | none |
| mealplan | templates, get, create, update, activate, client's active plan | delete |
| tracking | client: daily plan, track meal, track water; dietitian: progress | client-side endpoints for own profile, measurements and goal |

Cross-cutting gaps: no tests, no DB migrations (only the `dev` profile can start because the default profile uses `ddl-auto: validate`), no CI, no Dockerfile.

## Phase 0: Build and docs hygiene

- [x] Commit `gradle-wrapper.jar` so a fresh clone builds
- [x] Make the READMEs match what actually exists
- [x] Add this roadmap

## Phase 1: Harden the backend

- [ ] Invite flow: `POST /api/clients/{id}/invite` creates or regenerates a `ClientInvite` and returns the token (email delivery later)
- [ ] Flyway migrations for the whole schema; `validate` in every profile
- [ ] Integration tests with Testcontainers (auth, client isolation between dietitians, meal plan, tracking)
- [ ] GitHub Actions: build and test on every PR
- [ ] Client-facing endpoints under `/api/mobile`: me, my measurements, my goal, my progress
- [ ] Missing CRUD: measurement update/delete, meal plan delete, goal history
- [ ] Production config from environment variables, plus a Dockerfile

## Phase 2: Public demo API

- [ ] Deploy backend + PostgreSQL (Render, Fly.io or Railway)
- [ ] Seeded demo dietitian account with sample clients and plans
- [ ] Public Swagger UI link in the README

## Phase 3: Dietitian web panel (`web/`)

- [ ] Vite + React + TS scaffold, typed API client generated from OpenAPI
- [ ] Login, token refresh, protected routes
- [ ] Client list and detail pages, invite button
- [ ] Measurements table and weight/BMI charts
- [ ] Goal calculator form
- [ ] Meal plan builder (meals → alternatives → food items, live calorie/macro totals)
- [ ] Progress dashboard
- [ ] Deploy (Vercel or Netlify), demo login on the landing page

## Phase 4: Client mobile app (`mobile/`, KMP)

- [ ] Compose Multiplatform project (Android + iOS), Ktor client, kotlinx.serialization, Koin
- [ ] Accept invite and login, secure token storage
- [ ] Daily plan: pick one alternative per meal, mark meals done
- [ ] Water tracking
- [ ] Progress charts
- [ ] Offline cache of the current plan
- [ ] Android APK on GitHub Releases, iOS demo video

## Phase 5: Extras

- [ ] PDF measurement import (Apache PDFBox, Tarti.com template)
- [ ] Push notifications for meal and water reminders
- [ ] Screenshots and a short demo video in the README; update the GitHub repo description
