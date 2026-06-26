# Notes

## Time spent
Approximately 3 hours.

## What I built
Added a rate alert feature to the existing Spring Boot + Vue 3 app:
- `POST /api/alerts` — create an alert (pair, threshold, direction)
- `GET /api/alerts` — list all alerts with live triggered evaluation
- `DELETE /api/alerts/{id}` — delete an alert
- Alert UI wired into the existing frontend
- Swagger UI available at `/swagger-ui/index.html`

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
implementation. Also externalised the XE API base URL to
`application.properties`.

### Strict threshold comparison
Rate exactly at threshold = not triggered. "Above 1.38" means strictly
greater than, not greater than or equal. Covered by a dedicated test.

### Logging strategy
Used `info` for business events (alert created, deleted, triggered) and
`debug` for high-frequency technical details (rate fetches, evaluations).
Keeps production logs readable and meaningful.

### Validation
Added `@Valid` with `@NotBlank`, `@NotNull`, `@DecimalMin`, and `@Pattern`
on `CreateAlertRequest`. Invalid requests return a clean `400` JSON response
via `GlobalExceptionHandler`. Pair is restricted to the 3 supported pairs.

## Rough edges fixed
- `RatesController` copy-paste × 3 → extracted `RateService`
- `state.ts` used `any[]` → typed with `Rate` and `Alert` interfaces
- `App.vue` had 3 identical rate lookup functions → one `getRate(pair)` + `v-for`
- Externalised hardcoded XE API URL to `application.properties`

## What I would do with more time
- Persistent storage (PostgreSQL + JPA repository)
- Background scheduled job to evaluate alerts instead of at request time
- `MockMvc` tests for `AlertsController` HTTP status codes
- Frontend error handling and loading states
- Full CI/CD pipeline — build jar, Docker image, Trivy security scan, push to registry
- Override `commons-lang3` to `3.18.0` to resolve flagged CVE
- Extract `RateProvider` interface — allows swapping XE API for another provider without touching `AlertService`

## API documentation
Swagger UI: `http://localhost:5180/swagger-ui/index.html`

## AI tools used
Used Claude  to generate boilerplate, suggest test cases, and
review structure decisions. Every architectural decision — storage choice,
timestamp type, record vs class, evaluation approach, logging strategy —
was made and owned by me. AI accelerated the work; I drove the decisions.