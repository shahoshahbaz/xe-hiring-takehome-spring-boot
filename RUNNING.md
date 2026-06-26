# Running the Application

## Branches

| Branch | Description |
|--------|-------------|
| `main` | Original implementation — hardcoded pairs (USD/CAD, GBP/USD, EUR/USD), String direction |
| `feature/dynamic-currency-pairs` | Enhanced implementation — dynamic pairs from XE API, Direction enum, Jacoco coverage |

## Switching Branches

To see the original implementation:
```bash
git checkout main
```

To see the enhanced implementation:
```bash
git checkout feature/dynamic-currency-pairs
```

**Note:** Restart the backend after switching branches.

---

## Prerequisites
- JDK 21 (e.g. Eclipse Temurin 21)
- Node.js 20 or newer
- Maven Wrapper included (`./mvnw`) — no Maven install needed

## Quick Start (recommended)
From the project root:
```bash
chmod +x run.sh
./run.sh
```
This starts both backend and frontend together.

## Manual Start

### Backend
```bash
cd backend
./mvnw spring-boot:run
```
Backend runs on `http://localhost:5180`

### Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend runs on `http://localhost:5173`


## API Endpoints
## API Endpoints

### Rates
| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/api/rates` | Live rates for USD/CAD, GBP/USD, EUR/USD | Existing |
| GET | `/api/rates/pairs` | All supported currency pairs from XE API | **New** |

### Alerts
| Method | Path | Description | Status |
|--------|------|-------------|--------|
| GET | `/api/alerts` | List all alerts with triggered state | **New** |
| POST | `/api/alerts` | Create a new alert | **New** |
| DELETE | `/api/alerts/{id}` | Delete an alert | **New** |

## What Changed in `feature/dynamic-currency-pairs` vs `main`

### New files (feature branch only):
- `Direction.java` — enum replacing `String` direction, fixes Open/Closed SOLID violation
- `CurrencyResponse.java` — DTO for supported currencies from XE API

### Refactored files (both branches, enhanced in feature branch):
- `RateService.java` — added `getSupportedCurrencies()`, externalized URL config
- `AlertService.java` — added dynamic pair validation with cached currency list
- `RatesController.java` — added `GET /api/rates/pairs` endpoint
- `AlertResponse.java` — added `evaluationError` field
- `CreateAlertRequest.java` — replaced hardcoded `@Pattern` with `Direction` enum
- `App.vue` — dynamic currency dropdowns instead of hardcoded pairs
- `state.ts` — added `Currency` interface

### Same on both branches:
- `AlertsController.java`
- `Alert.java`
- `GlobalExceptionHandler.java`
- `AlertServiceTest.java`
## Project Structure

**Backend** `backend/src/main/java/com/xe/ratealerts/`

| File | Package | main | feature branch |
|------|---------|------|----------------|
| `RatesController.java` | controller | Refactored from original | Added `GET /api/rates/pairs` |
| `AlertsController.java` | controller | New | Unchanged |
| `RateService.java` | service | New — XE API wrapper | Added `getSupportedCurrencies()`, externalized URL config |
| `AlertService.java` | service | New — core alert logic | Added dynamic pair validation, cache |
| `Alert.java` | model | New | Uses `Direction` enum |
| `AlertResponse.java` | dto | New | Added `evaluationError` field |
| `CreateAlertRequest.java` | dto | New | Uses `Direction` enum, removed hardcoded `@Pattern` |
| `GlobalExceptionHandler.java` | exception | New | Added `IllegalArgumentException` handler |
| `Direction.java` | model | ❌ Not present | ✅ New — enum, fixes Open/Closed SOLID |
| `CurrencyResponse.java` | dto | ❌ Not present | ✅ New — supported currencies DTO |

**Frontend** `frontend/src/`

| File | main | feature branch |
|------|------|----------------|
| `App.vue` | Alert UI, hardcoded pairs | Dynamic currency dropdowns |
| `state.ts` | Typed interfaces, alerts state | Added `Currency` interface |

**Tests** `backend/src/test/java/com/xe/ratealerts/service/`

| File | main | feature branch |
|------|------|----------------|
| `AlertServiceTest.java` | 9 unit tests | Updated for Direction enum, lenient stubbing |