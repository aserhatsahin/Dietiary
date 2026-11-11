# Dietiary Roadmap

This file tracks where the project stands and what comes next.

## Decisions

- **Backend:** keep building on the existing Spring Boot / Java 17 / PostgreSQL code. We are not rewriting it.
- **Dietitian web panel:** React 18 + TypeScript, Vite, TailwindCSS, TanStack Query and Recharts.
- **Client mobile app:** Kotlin Multiplatform with Compose Multiplatform, targeting Android and iOS from one codebase.

The plan puts something people can try online first: a deployed API and web panel. The mobile app comes after that.

## Current state (backend)

| Module | Working endpoints | Missing |
|---|---|---|
| auth | register, login, refresh, accept-invite, invite a client | invite email delivery |
| client | list, get, create, update, delete, toggle active | none |
| measurement | list, latest, create, update, delete (BMI + deltas) | PDF import (only the enum exists) |
| goal | current goal, history, create (BMR/TDEE/calorie target) | none |
| food | search, categories, create, seed data | none |
| mealplan | templates, get, create, update, activate, delete, client's plans and active plan | none |
| tracking | client: daily plan, track meal, track water, own profile/measurements/goal/progress; dietitian: progress | none |

Cross-cutting gaps: startup is slow on a 0.1 CPU instance (about 2 minutes even with CDS); Spring AOT could shorten it further.

## Phase 0: Build and docs hygiene

- [x] Commit `gradle-wrapper.jar` so a fresh clone builds
- [x] Make the READMEs match what actually exists
- [x] Add this roadmap

## Phase 1: Harden the backend

- [x] Invite flow: `POST /api/clients/{id}/invite` creates or regenerates a `ClientInvite` and returns the token (email delivery later)
- [x] Return 403 (not 500) when a role is not allowed
- [x] Flyway migrations for the whole schema; `validate` in every profile
- [x] Integration tests with Testcontainers (auth, client isolation between dietitians, meal plan, tracking)
- [x] Bugs found by the integration tests: client logins had no `clientId` (every mobile endpoint failed), food search failed unless both filters were given, meal plan templates could not be saved, today's date was rejected for measurements, malformed requests returned 500
- [x] GitHub Actions: build and test on every PR
- [x] Client-facing endpoints under `/api/mobile`: me, my measurements, my goal, my progress
- [x] Missing CRUD: measurement update/delete, meal plan delete, goal history, a client's plan list
- [x] Measurement deltas compared against the previous measurement by date (they used the newest one)
- [x] Production config from environment variables, plus a Dockerfile and a CI job that runs the image
- [x] Security: no default JWT secret (the repo is public), weak secrets are rejected at startup; unknown URLs return 404 instead of 500
- [x] Upgrade Spring Boot 3.2 → 4.1 (3.x is out of support), Gradle 8.5 → 9.9

## Phase 2: Public demo API

- [x] Choose hosting: Render free web service + Neon free PostgreSQL (`render.yaml`, [docs/DEPLOY.md](DEPLOY.md))
- [x] System foods seeded by a Flyway migration, so every environment has them
- [x] Demo dietitian account with sample clients, plans and tracking (`APP_DEMO_ENABLED`)
- [x] Fit the 512 MB / 0.1 CPU free instance: JVM flags, CDS archive, keep-warm workflow
- [ ] Create the Neon project and the Render service (one-time, by the repo owner)
- [ ] Public demo and Swagger UI links in the README; update the GitHub repo description

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
