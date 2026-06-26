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

## What Changed in `main` vs original:

### New files:
- `AlertsController.java` — REST endpoints for alerts
- `RateService.java` — XE API wrapper, eliminates copy-paste
- `AlertService.java` — core alert logic, in-memory storage
- `Alert.java` — alert model
- `AlertResponse.java` — alert response DTO
- `CreateAlertRequest.java` — alert creation DTO
- `GlobalExceptionHandler.java` — clean JSON error responses

### Refactored files:
- `RatesController.java` — extracted HTTP logic to RateService
- `App.vue` — added alert UI, typed state, removed duplicate functions
- `state.ts` — typed interfaces replacing `any[]`
 
 ### Deleted files:
- `AlertsStubController.java` — removed stub controller, replaced with real `AlertsController.java`
## Project Structure

**Backend** `backend/src/main/java/com/xe/ratealerts/`

| File | Package | Status |
|------|---------|--------|
| `RatesController.java` | controller | Refactored — extracted to RateService |
| `AlertsController.java` | controller | New |
| `RateService.java` | service | New — XE API wrapper |
| `AlertService.java` | service | New — core alert logic |
| `Alert.java` | model | New |
| `AlertResponse.java` | dto | New |
| `CreateAlertRequest.java` | dto | New |
| `GlobalExceptionHandler.java` | exception | New |

**Frontend** `frontend/src/`

| File | Status |
|------|--------|
| `App.vue` | Refactored — alert UI wired |
| `state.ts` | Refactored — typed interfaces |

**Tests** `backend/src/test/java/com/xe/ratealerts/service/`

| File | Tests |
|------|-------|
| `AlertServiceTest.java` | 9 unit tests |