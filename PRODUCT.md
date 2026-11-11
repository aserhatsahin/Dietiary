# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

The dietitian panel described here is a web app. The client side is a separate mobile app (Kotlin Multiplatform, Android and iOS) with its own surface.

## Stack

React 18 + TypeScript (Vite), TanStack Query, against the existing Spring Boot API (`backend/`). Decided earlier in [docs/ROADMAP.md](docs/ROADMAP.md). Interface languages: English and Turkish, switchable in the app.

## Users

**Primary: a solo dietitian** running their own practice. They see clients both **in the clinic** (face to face, at a desk) and **online** (video calls, including clients in other cities). A typical practice follows tens of clients at once, each over weeks to months.

**Secondary: the dietitian's clients**, who never use this panel. They use the mobile app to follow their plan and log meals and water. The panel shows the dietitian what clients did there.

## Product Purpose

Dietiary digitizes the dietitian–client workflow that today lives in paper files, spreadsheets, scale printouts and messaging apps. The dietitian records measurements, sets goals, builds meal plans and follows each client's adherence in one place. The client follows the plan and logs what they ate in the app.

Success means a working dietitian can run real consultations with it: prepare before a session, record and discuss the new measurement during it, adjust the plan after it. Real use comes first. The project is also a portfolio piece, but that never justifies features or screens a dietitian would not use.

## Positioning

- **Flexible, alternative-based meal plans.** A plan is not tied to calendar days. Each meal has several alternatives prepared by the dietitian, and the client picks one each day, as dietitians actually write programs.
- **Closed-loop tracking.** What the client logs in the app (meals chosen and eaten, water) shows up in the panel as adherence, so the dietitian sees whether the plan is being followed, not only the scale result.
- **Calculations done for the dietitian.** BMI from each measurement; BMR, TDEE and a daily calorie target from the goal. No separate calculator or spreadsheet.
- **Body-analysis scale reports as input** (planned). Measurements read from the scale's PDF report instead of being typed in.

## Operating Context

- **Measurements come from two sources at each visit:**
  - a **body-analysis (bioimpedance) scale** report: weight, body fat (% and kg), body water (% and kg), muscle mass, bone mass, visceral fat, edema (extracellular water ratio), basal metabolic rate, metabolic age
  - **tape measurements:** circumferences such as waist, neck, hip, and others the dietitian tracks
- **Visit rhythm:** clients are typically measured weekly or every few weeks. Progress is judged over weeks, not days.
- **Online clients** measure at home or at a partner location and report the values; the dietitian enters them.
- **Plan building:** the dietitian assembles plans from a food library (system foods plus their own custom foods) and reuses templates across clients.

## Capabilities and Constraints

**Built (API live at https://dietiary-api.onrender.com):**
- clients: create, edit, deactivate, delete; invite to the app
- measurements: create, edit, delete; BMI and change since the previous measurement
- goals: history and active goal with BMR, TDEE, calorie target and macros
- food library: system and custom foods, categories, search
- meal plans and templates: meals, alternatives and food items with calorie totals; one active plan per client
- client tracking: daily plan, meals eaten, water
- dietitian progress view: weight trend, adherence, averages

**Required but not built yet:**
- **Fuller body composition in the data model:** the backend has weight, body fat %, muscle mass, water %, bone mass, visceral fat, BMR and metabolic age. It does not have fat mass, water mass, edema/extracellular water ratio, or **circumference measurements** (waist, neck, hip and others). These must exist in the API before the panel can show them.
- Scale PDF import (planned).
- Invite delivery by email (today the invite token is shared by hand).

**Terminology:**
- client, not patient
- meal plan
- meal, alternative (option)
- adherence: meals eaten vs. planned
- measurement
- goal

Turkish UI uses the dietitian's own words: danışan, ölçüm, beslenme planı, öğün, alternatif.

**Locale:** Turkish and English number, date and unit formats (decimal comma, `%83` in Turkish). Metric units only.

## Brand Commitments

- Name: **Dietiary**.
- Bilingual from the start: every user-facing string in English and Turkish.

No logo or other identity assets exist yet.

## Evidence on Hand

- Live demo API with seeded demo data: one demo dietitian, three clients with weekly measurements and goals, a meal plan with alternatives, a template, and four weeks of meal and water tracking for one client. See the README's live demo section.
- **Nothing else exists:** no real users, customers, testimonials, usage numbers or partner scales. Future work must not invent them.

## Product Principles

1. **Built for a working session.** Whatever the dietitian needs during a 30–45 minute consultation is at most one step away: the latest measurement, the change since last time, the goal, the plan and how well it was followed.
2. **Trends over snapshots.** One measurement means little; the panel always puts a value next to its previous value and its direction relative to the client's goal.
3. **The dietitian's numbers, completely.** Body composition and tape measurements are first-class data, not an optional extra field.
4. **Same truth for both sides.** The panel shows exactly what the client logged in the app, with no separate manual bookkeeping.
5. **Two languages, no second-class one.** Turkish is as complete and as correct as English, including formats.
