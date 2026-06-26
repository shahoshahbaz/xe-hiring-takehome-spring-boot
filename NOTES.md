# Notes

## Time spent
Approximately 5 hours (including feature branch enhancements).

## What I built

### `main` branch
Added a rate alert feature to the existing Spring Boot + Vue 3 app:
- `POST /api/alerts` — create an alert (pair, threshold, direction)
- `GET /api/alerts` — list all alerts with live triggered evaluation
- `DELETE /api/alerts/{id}` — delete an alert
- Alert UI wired into the existing frontend
- Swagger UI available at `/swagger-ui/index.html`

### `feature/dynamic-currency-pairs` branch
Extended the main implementation with:
- `GET /api/rates/pairs` — returns all supported currencies from XE API
- Dynamic pair validation — any XE-supported currency pair allowed
- `Direction` enum — replaces `String` direction, fixes Open/Closed SOLID violation
- `CurrencyResponse` DTO — includes iso, name, symbol
- `evaluationError` field on `AlertResponse` — graceful handling when rate fetch fails
- Frontend dynamic dropdown — populated from XE API instead of hardcoded
- Jacoco code coverage — 53% overall, 56% service layer
- Cached supported currencies — thread-safe lazy initialization
- 12 unit tests (up from 9 on main)

## Key decisions

### In-memory storage
Chose `ConcurrentHashMap` over H2/JPA. For a 2-3 hour exercise, adding a
persistence layer would have taken time away from the actual feature. The
`AlertService` abstraction means swapping to a JPA repository later only
touches the service and model — the controller stays unchanged.

### `Instant` over `LocalDateTime`
XE is a global currency platform. `Instant` is always UTC — no timezone
ambiguity. `LocalDateTime` carries no timezone info which is a real risk
in a multi-region financial system.

### Java record for `Alert`
Pure data container with no behaviour — immutability is correct here.
Would need to become an `@Entity` class if JPA persistence is added later.

### `triggered` not stored
Triggered state is a calculation, not data. The live rate changes constantly
so storing triggered would go stale immediately. Evaluated fresh on every
request in `AlertService.isTriggered()`.

### Extracted `RateService`
`RatesController` had the same HTTP call copy-pasted three times. Extracted
to `RateService` so both `RatesController` and `AlertService` share one
implementation. Also externalised the XE API config to
`application.properties` — base URL, version, and endpoints are all
independently configurable.

### Strict threshold comparison
Rate exactly at threshold = not triggered. "Above 1.38" means strictly
greater than, not greater than or equal. Covered by a dedicated test.

### Logging strategy
Used `info` for business events (alert created, deleted, triggered) and
`debug` for high-frequency technical details (rate fetches, evaluations).
Keeps production logs readable and meaningful.

### Validation
Added `@Valid` with `@NotBlank`, `@NotNull`, `@DecimalMin` on
`CreateAlertRequest`. Direction validated via enum — Jackson rejects invalid
values automatically. Pair validated dynamically against XE API supported
currencies (feature branch). Invalid requests return clean `400` JSON via
`GlobalExceptionHandler`.

### `Direction` enum
Replaced `String direction` with enum — fixes Open/Closed SOLID violation.
Adding a new direction (e.g. `EQUAL`) only requires a new enum case;
`isTriggered()` switch expression doesn't need modification.

### Dynamic pair validation
Currencies fetched from XE API and cached in memory on first request.
Thread-safe lazy initialization using `synchronized` block. Supports any
XE-supported currency pair including crypto (BTC, ETH etc) — though sandbox
API key only prices fiat pairs. Unsupported pairs return `evaluationError`
instead of crashing.

### Graceful rate fetch failure
If rate fetch fails (unsupported pair on sandbox, network issue), alert
evaluates to `triggered=false` and sets `evaluationError` field. App never
crashes — warn log captures the reason.

### Git strategy
Used `git cherry-pick` to share common commits (NOTES.md, RUNNING.md,
Postman collection, run.sh) across both branches without merging. Feature
branch left unmerged — in a real team this would go through code review
before merging to main.

## Rough edges fixed
- `RatesController` copy-paste × 3 → extracted `RateService`
- `state.ts` used `any[]` → typed with `Rate`, `Alert`, `Currency` interfaces
- `App.vue` had 3 identical rate lookup functions → one `getRate(pair)` + `v-for`
- Externalised hardcoded XE API URL to `application.properties`
- `AlertsStubController` deleted — replaced with real implementation

## SOLID review
- **S** ✅ — each class has one responsibility
- **O** ✅ — `Direction` enum + switch expression; adding new direction = new case only
- **L** ✅ — no inheritance used
- **I** ⚠️ — no interfaces on services; `RateProvider` interface would allow swapping XE API
- **D** ✅ — constructor injection throughout

## What I would do with more time
- Persistent storage (PostgreSQL + JPA repository)
- Background scheduled job to evaluate alerts instead of at request time
- `MockMvc` tests for `AlertsController` HTTP status codes
- Frontend error handling and loading states
- Full CI/CD pipeline — build jar, Docker image, Trivy security scan, push to registry
- Override `commons-lang3` to `3.18.0` to resolve flagged CVE
- Extract `RateProvider` interface — allows swapping XE API for another provider
- Custom `@Constraint` validator for pair validation with cached currency list
- Enable crypto pairs with a production API key (`crypto=true`)
- Pagination on `GET /api/rates/pairs`

## API documentation
Swagger UI: `http://localhost:5180/swagger-ui/index.html`

## How to Run

1. From the project root, run:
```bash
chmod +x run.sh
./run.sh
```
This starts both backend (port 5180) and frontend (port 5173).

2. Open the app: `http://localhost:5173`

3. API documentation: `http://localhost:5180/swagger-ui/index.html`

4. To test the API with Postman:
   - Open Postman
   - Click Import
   - Select `postman_collection.json` from the project root
   - Run the requests in order

## Additional Files
- `run.sh` — starts both backend and frontend with a single command
- `postman_collection.json` — Postman collection covering all API endpoints
- `RUNNING.md` — detailed setup instructions, project structure, and branch guide

## AI tools used
Used Claude to generate boilerplate, suggest test cases, and review
structure decisions. Every architectural decision — storage choice,
timestamp type, record vs class, evaluation approach, logging strategy,
SOLID review — was made and owned by me. AI accelerated the work; I drove
the decisions.