# XE Take-Home — Panel Review Q&A

Everything discussed during the build. Use this to prepare for the follow-up conversation.

---

## Table of Contents

**Project Overview**
- [1. What is the app doing and what did you build?](#1-what-is-the-app-doing-and-what-did-you-build)
- [2. Why in-memory storage and not H2 or a real DB?](#2-why-in-memory-storage-and-not-h2-or-a-real-db)
- [3. Why ConcurrentHashMap and not just HashMap?](#3-why-concurrenthashmap-and-not-just-hashmap)
- [8. What is a currency pair?](#8-what-is-a-currency-pair)
- [9. Package structure and why](#9-package-structure-and-why)

**Data Model**
- [4. What are the Alert fields and why?](#4-what-are-the-alert-fields-and-why)
- [5. What is a Java Record and why use it?](#5-what-is-a-java-record-and-why-use-it)
- [6. What if you need to add a DB later — does the record break?](#6-what-if-you-need-to-add-a-db-later--does-the-record-break)
- [7. Why Instant and not LocalDateTime for createdAt?](#7-why-instant-and-not-localdatetime-for-createdAt)
- [29. Why BigDecimal for threshold instead of double or float?](#29-why-did-you-use-bigdecimal-for-threshold-instead-of-double-or-float)

**DTOs & Validation**
- [30. Why these specific validations on CreateAlertRequest?](#30-why-these-specific-validations-on-createalertrequest)
- [31. What does @JsonValue do in the Direction enum?](#31-what-does-jsonvalue-do-in-the-direction-enum)
- [32. Why is triggered not stored, and why does evaluationError exist?](#32-why-is-triggered-not-stored-and-why-does-evaluationerror-exist)
- [10. Why is triggered not stored on the Alert model?](#10-why-is-triggered-not-stored-on-the-alert-model)

**Services & Business Logic**
- [16. Why did you extract RatesController into RateService?](#16-why-did-you-extract-ratescontroller-into-rateservice)
- [17. Why externalize the XE API URL to application.properties?](#17-why-externalize-the-xe-api-url-to-applicationproperties)
- [18. Why does AlertsController stay thin?](#18-why-does-alertscontroller-stay-thin)
- [26. What happens when rate fetch fails for a pair like BTC/CAD?](#26-what-happens-when-rate-fetch-fails-for-a-pair-like-btccad)

**SOLID & Design**
- [23. What SOLID violations did you find and fix?](#23-what-solid-violations-did-you-find-and-fix)
- [24. Why use a switch expression instead of if/else for direction?](#24-why-use-a-switch-expression-instead-of-ifelse-for-direction)

**Testing & Coverage**
- [12. What tests did you write and why those specifically?](#12-what-tests-did-you-write-and-why-those-specifically)
- [19. Mockito UnnecessaryStubbingException — what happened and why?](#19-mockito-unnecessarystubbingexception--what-happened-and-why)
- [20. What does deleted_alert_removed_from_list verify?](#20-what-does-deleted_alert_removed_from_list-verify)
- [27. What is Jacoco and what did your coverage report show?](#27-what-is-jacoco-and-what-did-your-coverage-report-show)
- [28. What is the Mockito inline mock maker warning?](#28-what-is-the-mockito-inline-mock-maker-warning)

**Logging**
- [22. Why did you use debug vs info in logging?](#22-why-did-you-use-debug-vs-info-in-logging)

**Frontend**
- [21. Frontend changes — what and why](#21-frontend-changes--what-and-why)
- [39. What is `state.ts` and why does it exist?](#39-what-is-statets-and-why-does-it-exist)
- [40. Vue concepts used in `App.vue` — where are they?](#40-vue-concepts-used-in-appvue--where-are-they)
- [41. `App.vue` — short summary of each section](#41-appvue--short-summary-of-each-section)

**Git & Process**
- [13. Commit history strategy](#13-commit-history-strategy)
- [25. Why did you use git cherry-pick and what does it do?](#25-why-did-you-use-git-cherry-pick-and-what-does-it-do)

**Rough Edges & What's Next**
- [11. What rough edges did you fix in the existing code?](#11-what-rough-edges-did-you-fix-in-the-existing-code)
- [14. What would you do with more time?](#14-what-would-you-do-with-more-time)
- [15. How did you use AI tools?](#15-how-did-you-use-ai-tools)

**Scalability & Production**
- [42. How would you scale this app to handle millions of alerts?](#42-how-would-you-scale-this-app-to-handle-millions-of-alerts)
- [43. What happens if the XE API goes down?](#43-what-happens-if-the-xe-api-goes-down)
- [44. How would you handle 10,000 concurrent users?](#44-how-would-you-handle-10000-concurrent-users)
- [45. How would you make alert evaluation real-time instead of on-request?](#45-how-would-you-make-alert-evaluation-real-time-instead-of-on-request)
- [46. How would you add a database?](#46-how-would-you-add-a-database)
- [47. Which database would you choose and why?](#47-which-database-would-you-choose-and-why)
- [48. What's missing before this goes to production?](#48-whats-missing-before-this-goes-to-production)
- [49. The currency cache never expires — what's the problem and how do you fix it?](#49-the-currency-cache-never-expires--whats-the-problem-and-how-do-you-fix-it)
- [50. How would you add authentication?](#50-how-would-you-add-authentication)
- [51. How would you monitor it?](#51-how-would-you-monitor-it)
- [52. What happens to alerts when the server restarts?](#52-what-happens-to-alerts-when-the-server-restarts)
- [53. How would you add multi-user support?](#53-how-would-you-add-multi-user-support)
- [54. What if `GET /api/alerts` is called 1000 times per second?](#54-what-if-get-apialerts-is-called-1000-times-per-second)
- [55. How would you reduce XE API calls?](#55-how-would-you-reduce-xe-api-calls)

---

## 1. What is the app doing and what did you build?

The existing app fetches live exchange rates (USD/CAD, GBP/USD, EUR/USD) from the XE Currency Data API and displays them.

I added a **Rate Alert feature**:
- A user defines a threshold for a currency pair and a direction (above/below)
- The app evaluates each alert against the live rate and shows whether it has triggered

**Three things built:**
1. Backend endpoints — POST / GET / DELETE `/api/alerts`
2. Alert evaluation logic — compare threshold against live rate at request time
3. Frontend UI — create form, alert list, triggered indicator, delete button

---

## 2. Why in-memory storage and not H2 or a real DB?

The assignment explicitly says *"in-memory is acceptable if you explain the trade-off."*

**Chose plain in-memory (`ConcurrentHashMap`) because:**
- Simpler — no JPA, no schema, no setup overhead
- The stub they provided already used `CopyOnWriteArrayList` — signalling expected approach
- Spending 30+ minutes on H2 setup would take time away from the actual feature

**Trade-off acknowledged:**
- Alerts are lost on restart
- Not suitable for production

**What I would do with more time:**
- Add PostgreSQL with a JPA repository layer
- The `AlertService` abstraction means the controller stays untouched — only the service and model change

---

## 3. Why `ConcurrentHashMap` and not just `HashMap`?

Spring Boot handles multiple HTTP requests concurrently. A plain `HashMap` is not thread-safe — two requests hitting it simultaneously could corrupt data or throw exceptions.

`ConcurrentHashMap` is thread-safe without needing `synchronized` blocks, making it the correct choice for shared in-memory state in a web application.

---

## 4. What are the Alert fields and why?

```java
public record Alert(
    UUID id,
    String pair,
    BigDecimal threshold,
    String direction,
    Instant createdAt
)
```

- `id` → needed to identify the alert for DELETE
- `pair` → which currency pair is being watched (e.g. "USD/CAD")
- `threshold` → the rate value to compare against (e.g. 1.84)
- `direction` → "above" or "below"
- `createdAt` → good practice; useful for ordering and auditing

**How I knew these fields:** the README defined the API contract, and the stub controller already modelled them.

---

## 5. What is a Java Record and why use it?

A `record` is a special Java class (Java 16+) for holding **immutable data**. It auto-generates:
- Constructor
- Getters
- `equals()`, `hashCode()`, `toString()`

Instead of writing 30+ lines of boilerplate, you write:
```java
public record Alert(UUID id, String pair, BigDecimal threshold, String direction, Instant createdAt) {}
```

**Why use it here:**
- `Alert` is pure data storage — immutability is correct
- Less code = less to explain
- Java 21 is in the tech stack — using records shows you actually use modern Java
- XE's own stub already used records

---

## 6. What if you need to add a DB later — does the record break?

Yes. Records don't work with JPA/Hibernate because:
- JPA requires a no-arg constructor
- JPA requires mutable fields (setters)
- Records are immutable

**What changes if DB is added:**
- `Alert` record → `@Entity` class with `@Id`, `@GeneratedValue`
- `AlertService` → uses JPA Repository instead of `ConcurrentHashMap`
- `AlertsController` → **unchanged** (abstraction protects it)

This is good architecture — only the model and service layer change.

---

## 7. Why `Instant` and not `LocalDateTime` for `createdAt`?

| Type | Timezone | Readable |
|------|----------|----------|
| `LocalDateTime` | ❌ No timezone info | ✅ Yes |
| `Instant` | ✅ Always UTC | Somewhat |
| `ZonedDateTime` | ✅ Timezone-aware | ✅ Yes |

**Chose `Instant` because:**
- XE is a global currency platform — timezone ambiguity is a real risk
- `Instant` is always UTC — unambiguous across systems
- Financial systems require unambiguous timestamps (same reasoning applies at Moneris)

> "Used `Instant` over `LocalDateTime` — in a global currency platform timezone ambiguity is a real risk."

---

## 8. What is a currency pair?

A pair = two currencies being compared.

- `USD/CAD` = how many Canadian dollars does 1 US dollar buy?
- `GBP/USD` = how many US dollars does 1 British pound buy?
- `EUR/USD` = how many US dollars does 1 Euro buy?

Left side = converting **from**, right side = what you **get**.

**Example alert:**
```json
{ "pair": "USD/CAD", "threshold": 1.40, "direction": "above" }
```
> "Tell me when 1 US dollar buys more than 1.40 Canadian dollars"
- Live rate 1.42 → triggered ✅
- Live rate 1.38 → not triggered ❌

---

## 9. Package structure and why

```
com.xe.ratealerts/
├── controller/
│   ├── RatesController.java        (refactored)
│   └── AlertsController.java       (new - replaces stub)
├── service/
│   ├── RateService.java            (new - XE API logic)
│   └── AlertService.java           (new - core alert logic)
├── model/
│   └── Alert.java                  (new - internal data)
└── dto/
    ├── CreateAlertRequest.java     (new - POST body)
    └── AlertResponse.java          (new - includes triggered)
```

**Why separate model from DTO:**
- `Alert` (model) = what we store. No `triggered` field — triggered is calculated, not stored
- `CreateAlertRequest` (DTO) = what client sends. No `id`, no `createdAt` — server assigns those
- `AlertResponse` (DTO) = what we return. Includes `triggered` for the UI

---

## 10. Why is `triggered` not stored on the Alert model?

Because `triggered` is **not state — it's a calculation**.

At any moment the live rate changes, the triggered status changes too. Storing it would mean it goes stale immediately.

**Correct approach:** evaluate `triggered` at request time by comparing the alert's threshold against the current live rate.

```java
boolean triggered = "above".equals(alert.direction())
    ? liveRate.compareTo(alert.threshold()) > 0
    : liveRate.compareTo(alert.threshold()) < 0;
```

---

## 11. What rough edges did you fix in the existing code?

`RatesController` had the same HTTP call copy-pasted 3 times (USD/CAD, GBP/USD, EUR/USD) — same credentials setup, same parsing logic, repeated verbatim.

**Fixed by extracting `RateService`:**
- One method handles any currency pair
- `RatesController` calls it 3 times cleanly
- `AlertService` reuses it for evaluation — no duplication

---

## 12. What tests did you write and why those specifically?

Focused on `AlertService` — the core logic. Tests cover:

- Alert triggered when rate is **above** threshold ✅
- Alert not triggered when rate is **below** threshold ✅
- Alert triggered when rate is **below** threshold (below direction) ✅
- Alert not triggered when rate is **above** threshold (below direction) ✅
- Rate exactly **at** threshold — not triggered (strict comparison) ✅
- Create alert → appears in list ✅
- Delete alert → removed from list ✅
- Delete unknown id → returns 404 ✅

**Why these:** the evaluation logic is the core of the feature. If that's wrong, nothing else matters.

---

## 13. Commit history strategy

```
commit 1: initial commit (from XE)
commit 2: add Alert model and DTOs
commit 3: add RateService - extract XE API logic
commit 4: add AlertService - core alert evaluation logic
commit 5: add AlertsController - replace stub
commit 6: refactor RatesController to use RateService
commit 7: add AlertService tests
commit 8: wire alert UI into App.vue
commit 9: add NOTES.md
commit 10: add GitHub Actions CI workflow (optional)
```

Small, honest, incremental — tells the story of the work.

---

## 14. What would you do with more time?

- Persistent storage (PostgreSQL + JPA)
- Input validation with `@Valid` and `@NotNull`
- Alert evaluation on a scheduled background job (not just at request time)
- Support for any currency pair from the XE API, not just the 3 hardcoded ones
- Pagination on GET /api/alerts
- Frontend error handling and loading states

---

## 15. How did you use AI tools?

Used Claude (via Cursor) to:
- Generate boilerplate (records, DTOs)
- Suggest test cases
- Review structure decisions

What I kept vs rejected — evaluated every suggestion against the assignment requirements and my own architectural judgement. AI accelerated the work; I drove the decisions.

---

## 16. Why did you extract `RatesController` into `RateService`?

The original `RatesController` had the exact same 8 lines of HTTP call code copy-pasted 3 times — once for USD/CAD, once for GBP/USD, once for EUR/USD.

**Problems with copy-paste:**
- If the XE API changes, you update it in 3 places instead of 1
- Creates bugs — easy to change one copy and forget the others
- Harder to test — can't mock a private method inside a controller

**Fix:** extracted `RateService` with one `getMidRate()` method. `RatesController` calls it 3 times cleanly. `AlertService` also reuses it — no duplication.

---

## 17. Why externalize the XE API URL to `application.properties`?

Hardcoded URLs are bad practice:
- If XE changes their API URL you have to find it in code and recompile
- Can't override per environment (dev/staging/prod)

Added `xecd.base-url` to `application.properties` and injected via `@Value` in `RateService`. Same pattern used at Moneris with production configs.

---

## 18. Why does `AlertsController` stay thin?

All logic is in `AlertService`. The controller only:
- Receives the HTTP request
- Calls the service
- Returns the right HTTP status (201 Created, 204 No Content, 404 Not Found)

**Why:** easy to test service independently of HTTP. If you swap REST for GraphQL later, the service doesn't change.

---

## 19. Mockito `UnnecessaryStubbingException` — what happened and why?

Had this in `@BeforeEach`:
```java
when(rateService.getMidRate("USD/CAD")).thenReturn(new BigDecimal("1.3800"));
```

But `delete_returns_false_for_unknown_id` never calls `getMidRate` — it just tries to delete a non-existent UUID. Mockito threw `UnnecessaryStubbingException`.

**Why Mockito does this:** strict mode treats unused mocks as a code smell — you either set up something you don't need, or you're not testing what you think you're testing.

**Fix:** moved mock into each individual test that needs it. `delete_returns_false_for_unknown_id` has no mock — it doesn't need rates at all.

**Rule:** use `@BeforeEach` mock only when **every single test** needs it.

---

## 20. What does `deleted_alert_removed_from_list` verify?

Two things:
1. `delete()` returns `true` when alert exists
2. `listAll()` is empty after deletion

Tests the **state change** — not just the return value. An alert that returns `true` on delete but still appears in the list would be a bug.

---

## 21. Frontend changes — what and why

### `state.ts` changes:
1. Added proper `Rate` and `Alert` interfaces — replaced `any[]` with typed arrays
2. Added `alerts: [] as Alert[]` — new state to hold alerts

### `App.vue` changes:

**What you must understand for the follow-up:**

1. **Why you replaced 3 functions with one `getRate(pair)`** — same logic repeated 3 times, DRY principle. Same reason you extracted `RateService` on the backend.

2. **Why `ref()` for form fields** — reactive form inputs in Vue. When the value changes, the UI updates automatically.

3. **Why `loadAlerts()` on mount** — load existing alerts when page opens, same as `loadRates()`.

4. **Why triggered alerts are highlighted** — `:class="{ triggered: alert.triggered }"` — conditional CSS driven by the `triggered` boolean coming from the backend. The frontend doesn't calculate anything — it just displays what the backend says.

5. **The three fetch calls:**
   - `POST /api/alerts` — create alert with pair, threshold, direction
   - `GET /api/alerts` — list all alerts including triggered state
   - `DELETE /api/alerts/{id}` — remove alert by id

**What you say if asked about the frontend:**
> "I kept it minimal — the assignment said they're not judging CSS. I focused on wiring the three endpoints correctly: create, list with triggered state, and delete. I also refactored the rate cards from three hardcoded blocks to a v-for loop since it was the same pattern repeated — same DRY principle I applied on the backend with RateService."

**Key rough edge fixed:**
Original had `getUsdCad()`, `getGbpUsd()`, `getEurUsd()` — same loop copy-pasted 3 times. Replaced with one `getRate(pair)` and a `v-for` loop.

---

## 22. Why did you use `debug` vs `info` in logging?

**The rule:**
- `log.info` → important business events you always want to see
- `log.debug` → detailed technical info, too noisy for production
- `log.warn` → something unexpected but handled
- `log.error` → something failed

**Applied in this project:**

| Method | Level | Why |
|--------|-------|-----|
| `create()` | `info` | Business event — alert was created |
| `delete()` success | `info` | Business event — alert was deleted |
| `delete()` not found | `warn` | Unexpected but handled |
| `getMidRate()` | `debug` | Called constantly — too noisy for info |
| `isTriggered()` triggered | `info` | Something happened worth knowing |
| `isTriggered()` not triggered | `debug` | Routine, nothing to report |
| `getMidRate()` failure | `error` | Something failed |

**Why not all `info`?**

In production, `GET /api/alerts` could be called hundreds of times per minute.
Every rate fetch and evaluation at `info` level = unreadable noise.
Real events like "alert created" get buried.

`debug` logs are off by default in production — turned on only when debugging a specific issue.

**What you say in the follow-up:**
> "I used debug for anything that happens on every request — rate fetches, routine evaluations. Info for actual business events — alert created, deleted, triggered. That way production logs stay readable and meaningful."

---

## 23. What SOLID violations did you find and fix?

**S — Single Responsibility ✅**
- `RateService` — only fetches rates
- `AlertService` — only manages alerts
- `AlertsController` — only handles HTTP
- `GlobalExceptionHandler` — only handles errors

**O — Open/Closed ⚠️ → Fixed**

Original code:
```java
boolean triggered = "above".equals(alert.direction()) ? comparison > 0 : comparison < 0;
```
Problem: if you add a new direction like "equal", you have to **modify** `isTriggered()`. That violates Open/Closed — open for extension, closed for modification.

**Fix — `Direction` enum + switch expression:**
```java
boolean triggered = switch (alert.direction()) {
    case ABOVE -> comparison > 0;
    case BELOW -> comparison < 0;
};
```
Now adding a new direction = add a new enum case. `isTriggered()` doesn't change.

**L — Liskov Substitution ✅** Not applicable — no inheritance

**I — Interface Segregation ⚠️ — noted but not fixed**
No interfaces on services. With more time would extract `RateProvider` interface so `AlertService` depends on abstraction not concrete class. Left as NOTES.md item.

**D — Dependency Inversion ✅**
`AlertService` depends on `RateService` via constructor injection. Spring manages the wiring.

---

## 24. Why use a switch expression instead of if/else for direction?

```java
boolean triggered = switch (alert.direction()) {
    case ABOVE -> comparison > 0;
    case BELOW -> comparison < 0;
};
```

Three reasons:
1. **Exhaustive** — compiler forces you to handle every enum case. If you add `EQUAL` to the enum and forget to add it to the switch, it won't compile.
2. **Cleaner** — no boolean variable, no if/else nesting
3. **Open/Closed** — extending behaviour means adding a case, not modifying existing logic

---

## 25. Why did you use `git cherry-pick` and what does it do?

**The situation:**
- `NOTES.md` was committed on `main` after the feature branch was created
- The feature branch needed `NOTES.md` too
- But I didn't want to merge `main` into the feature branch

**What `cherry-pick` does:**
Takes a specific commit from one branch and applies it to another branch — without bringing in any other commits.

```bash
# get the commit hash from main
git log --oneline -1
# output: abc1234 add NOTES.md

# apply just that commit to the feature branch
git cherry-pick abc1234
```

**Why not merge?**
- Merging would bring ALL of main's commits into the feature branch
- Cherry-pick is surgical — only takes exactly what you need
- Keeps the branch history clean and focused

**What you say in the panel:**
> "I missed committing NOTES.md before creating the feature branch. Rather than merging main into the feature branch, I used cherry-pick to bring just that specific commit across. It's cleaner — merge would have muddied the branch history."

---

## 26. What happens when rate fetch fails for a pair like BTC/CAD?

**The problem:**
- Sandbox API key doesn't support crypto rate fetching
- `BTC` passes currency validation (it's in the currencies list)
- But `getMidRate("BTC/CAD")` throws `RuntimeException`
- Without handling → 500 error returned to client

**The fix:**
Catch `RuntimeException` in `isTriggered()` and return `false` instead of crashing:

```java
boolean isTriggered(Alert alert) {
    try {
        BigDecimal liveRate = rateService.getMidRate(alert.pair());
        // ... evaluation logic
    } catch (RuntimeException e) {
        log.warn("Could not evaluate alert for pair={}: {}", alert.pair(), e.getMessage());
        return false;
    }
}
```

**Why `false` and not re-throw?**
- The alert itself is valid — the pair exists in XE's currency list
- The failure is a runtime/account limitation, not a user error
- Returning `false` (not triggered) is safe — better than crashing the entire list response

**What you say in the panel:**
> "If the rate fetch fails — network issue, unsupported pair on this account — the alert evaluates to not triggered rather than crashing. The warn log captures the reason. With more time I'd add an `evaluationError` field to `AlertResponse` so the UI can show the user why the alert can't be evaluated."

---

## 27. What is Jacoco and what did your coverage report show?

**What Jacoco is:**
Jacoco is a Java code coverage tool — it measures which lines, branches, and methods are executed during tests. Added as a Maven plugin; generates an HTML report at `target/site/jacoco/index.html` after `mvn test`.

**Coverage results:**

| Package | Coverage | Why |
|---------|----------|-----|
| `dto` | 100% | Records fully covered by service tests |
| `model` | 93% | Alert record and Direction enum well covered |
| `service` | 56% | AlertService tested; RateService is HTTP adapter |
| `controller` | 19% | No MockMvc tests yet |
| `exception` | 11% | GlobalExceptionHandler not directly tested |
| **Total** | **53%** | |

**What you say in the panel:**
> "53% overall — core business logic in `AlertService` is well covered. `RateService` is an HTTP adapter — testing it meaningfully requires integration tests against the real XE API. Controller and exception handler coverage would come from `MockMvc` tests which I noted as a next step."

**Why not aim for 100%?**
- Chasing 100% coverage often leads to meaningless tests
- Better to have fewer high-quality tests on core logic than many shallow tests padding the number
- The untested areas are either HTTP adapters or Spring framework code — not business logic

---

## 28. What is the Mockito inline mock maker warning?

```
Mockito is currently self-attaching to enable the inline-mock-maker. 
This will no longer work in future releases of the JDK.
```

**What it means:**
- Not a test failure — all tests still pass
- Mockito uses a self-attaching agent to enable inline mocking
- Future JDK versions will restrict this for security reasons

**What you say in the panel:**
> "That's a Mockito inline mock maker warning — it's a JDK compatibility notice, not a test failure. In production I'd add the Mockito agent configuration to `pom.xml` to suppress it."

**The fix (if needed):**
Add to `pom.xml` surefire plugin configuration:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>-javaagent:${settings.localRepository}/org/mockito/mockito-core/${mockito.version}/mockito-core-${mockito.version}.jar</argLine>
    </configuration>
</plugin>
```

---

## 29. Why did you use `BigDecimal` for threshold instead of `double` or `float`?

Because we're dealing with **financial/currency data** and floating point types have precision issues.

**Example of the problem with `double`:**
```java
double a = 1.38;
double b = 1.37;
System.out.println(a - b); // prints 0.010000000000000009  ← WRONG
```

**With `BigDecimal`:**
```java
BigDecimal a = new BigDecimal("1.38");
BigDecimal b = new BigDecimal("1.37");
System.out.println(a.subtract(b)); // prints 0.01  ← CORRECT
```

**Why this matters for alerts:**
If threshold is `1.3800` and live rate is `1.3800`:
- With `double` — comparison might return wrong result due to floating point rounding
- With `BigDecimal` — exact comparison, always correct

**This is especially important at XE** — they process billions of currency transactions. A rounding error in financial calculations can mean real money lost.

**What you say in the panel:**
> "Used `BigDecimal` for the threshold — `double` and `float` have floating point precision issues that are unacceptable in financial calculations. `BigDecimal` gives exact decimal arithmetic. This is standard practice in any fintech system."

---

## 30. Why these specific validations on `CreateAlertRequest`?

```java
@NotBlank(message = "Pair is required e.g. USD/CAD")
String pair,

@NotNull(message = "Threshold is required")
@DecimalMin(value = "0.0001", message = "Threshold must be greater than 0")
BigDecimal threshold,

@NotNull(message = "Direction is required - must be 'above' or 'below'")
Direction direction
```

**`pair` — `@NotBlank`:**
- Rejects null, empty string, and whitespace-only
- Actual pair validation done dynamically in `AlertService` against XE API
- On main branch had `@Pattern` hardcoding valid pairs — removed on feature branch in favour of dynamic validation

**`threshold` — `@NotNull` + `@DecimalMin`:**
- `@NotNull` — threshold must be provided
- `@DecimalMin("0.0001")` — must be greater than zero. A threshold of 0 or negative makes no financial sense
- `BigDecimal` not `double` — financial precision

**`direction` — `@NotNull`:**
- No `@Pattern` needed — it's an enum
- Jackson automatically rejects any value that's not `"above"` or `"below"`
- `@NotNull` just ensures direction was provided at all

**Why validation on the DTO and not in the service?**
- Fail fast — reject bad input at the HTTP layer before it reaches business logic
- Clean separation — DTO handles input validation, service handles business logic
- Standard Spring practice — `@Valid` on the controller method triggers all annotations automatically

**What you say in the panel:**
> "Validation is on the DTO — fail fast at the HTTP boundary. `@NotBlank` for pair, `@NotNull` + `@DecimalMin` for threshold to reject zero or negative values, and `@NotNull` for direction — the enum itself handles value validation since Jackson rejects anything that's not `above` or `below`."

---

## 31. What does `@JsonValue` do in the `Direction` enum?

It tells Jackson (the JSON serializer) to use the `getValue()` method when serializing the enum to JSON.

**Without `@JsonValue`:**
```json
{ "direction": "ABOVE" }  ← enum name in uppercase
```

**With `@JsonValue`:**
```json
{ "direction": "above" }  ← lowercase string value
```

**The full picture:**
- **Deserialization** (client → server): Jackson maps `"above"` → `Direction.ABOVE` automatically
- **Serialization** (server → client): `@JsonValue` ensures `Direction.ABOVE` → `"above"`

Keeps the API contract consistent — the frontend sends `"above"` and gets `"above"` back.

**What you say in the panel:**
> "`@JsonValue` tells Jackson to use the lowercase string value when serializing the enum — so the API returns `"above"` not `"ABOVE"`. Keeps the API contract consistent with what the frontend sends."

---

## 32. Why is `triggered` not stored, and why does `evaluationError` exist?

**Why `triggered` is not stored:**

`triggered` is not a property of the alert — it's a **calculation** based on the live rate at that moment.

The live rate changes constantly. If you stored `triggered = true` at 2pm, by 2:05pm the rate may have moved and the alert is no longer triggered. Your stored value is already stale.

**Correct approach:** evaluate `triggered` fresh on every request by comparing the alert's threshold against the current live rate.

```java
boolean triggered = switch (alert.direction()) {
    case ABOVE -> liveRate.compareTo(alert.threshold()) > 0;
    case BELOW -> liveRate.compareTo(alert.threshold()) < 0;
};
```

Think of it like a dashboard — every time you refresh, you see the current state, not a snapshot from the past.

**Why `evaluationError` exists:**

Sometimes the rate fetch fails — network issue, unsupported pair on sandbox (like BTC/CAD), API timeout.

Without `evaluationError`:
- Option 1: throw exception → crashes the entire alerts list response
- Option 2: silently return `triggered=false` → user has no idea why

With `evaluationError`:
- Return `triggered=false` safely
- Set `evaluationError = "Rate unavailable for pair: BTC/CAD"`
- UI shows ⚠️ instead of just "Watching"
- App never crashes

**What you say in the panel:**
> "`triggered` is a calculation not state — storing it would go stale immediately as rates move. `evaluationError` was added to handle graceful failures — if the rate fetch fails for any reason, the alert returns `triggered=false` with an error message rather than crashing the response. The UI can surface that to the user."

---

## 33. How is the XE API called in `RateService` and why Base64 encoding?

`RateService` wraps the XE API using Spring's `RestTemplate`. Credentials are Base64-encoded (`username:password`) in the constructor once — Basic Auth standard (RFC 7617), not encryption, real security comes from HTTPS. The URL is built from externalized config (`baseUrl + version + endpoint`). Jackson parses the JSON response, extracting `to[0].mid` scaled to 4 decimal places with `HALF_UP` rounding. Any failure is caught, logged as error, and re-thrown as `RuntimeException` for `GlobalExceptionHandler` to handle.

**What you say in the panel:**
> "RateService wraps the XE API — Basic Auth credentials built once in the constructor as Base64, `RestTemplate` for the HTTP call, Jackson for JSON parsing. URL fully externalized — base URL, version, and endpoints all in `application.properties` so a version bump is one config change."

---

## 34. What HTTP status codes does the API return and why?

| Method | Endpoint | Status Code | When |
|--------|----------|-------------|------|
| GET | `/api/alerts` | 200 OK | Always — returns empty list if no alerts |
| GET | `/api/rates` | 200 OK | Always |
| GET | `/api/rates/pairs` | 200 OK | Always |
| POST | `/api/alerts` | 201 Created | Alert created successfully |
| POST | `/api/alerts` | 400 Bad Request | Null threshold, invalid direction, unsupported pair |
| DELETE | `/api/alerts/{id}` | 204 No Content | Alert deleted successfully |
| DELETE | `/api/alerts/{id}` | 404 Not Found | Alert ID doesn't exist |
| Any | Any | 500 Internal Server Error | XE API call fails unexpectedly |

**Why these matter:**
- 201 vs 200 — tells the client a resource was created, not just a successful operation
- 204 vs 200 — tells the client there's no body to parse
- 400 vs 500 — client error vs server error, important distinction for API consumers

---

## 35. `CreateAlertRequest.java` — why each validation annotation?

```java
public record CreateAlertRequest(
        @NotBlank(message = "Pair is required e.g. USD/CAD")
        String pair,

        @NotNull(message = "Threshold is required")
        @DecimalMin(value = "0.0001", message = "Threshold must be greater than 0")
        BigDecimal threshold,

        @NotNull(message = "Direction is required - must be 'above' or 'below'")
        Direction direction
)
```

**`@NotBlank` on pair:**
- Rejects null, empty string, and whitespace-only strings
- `@NotNull` alone would allow empty string `""` — `@NotBlank` is stricter
- Used `@NotBlank` not `@NotNull` because pair is a String

**`@NotNull` on threshold:**
- `BigDecimal` can be null if client sends `"threshold": null`
- `@NotBlank` doesn't work on `BigDecimal` — only on Strings
- So `@NotNull` is the correct annotation here

**`@DecimalMin(value = "0.0001")`:**
- Prevents zero or negative thresholds — meaningless for a rate alert
- Uses `BigDecimal` string value for precision — not a double

**`@NotNull` on direction (not `@NotBlank`):**
- `Direction` is an enum, not a String — can't be blank
- `@NotNull` checks it's not null
- Jackson automatically rejects invalid enum values (e.g. `"sideways"`) with a 400
- So `@NotNull` + enum type = complete validation with no `@Pattern` needed

**Why no `@Pattern` on pair in feature branch:**
- Pair validation moved to `AlertService.isValidPair()` — checks dynamically against XE API
- Hardcoded `@Pattern` would break whenever XE adds new currencies

---

## 36. `AlertService.java` — walk through every method

**`create()`:**
```java
public AlertResponse create(CreateAlertRequest request)
```
- Validates pair dynamically via `isValidPair()` — throws `IllegalArgumentException` if invalid
- Creates `Alert` record with server-assigned `UUID` and `Instant.now()`
- Stores in `ConcurrentHashMap`
- Returns `AlertResponse` — which includes `triggered` evaluated immediately

**`listAll()`:**
```java
public List<AlertResponse> listAll()
```
- Streams all alerts from the map
- Converts each to `AlertResponse` via `toResponse()` — evaluates `triggered` live
- Every call fetches fresh rates — no stale triggered state

**`delete()`:**
```java
public boolean delete(UUID id)
```
- `ConcurrentHashMap.remove()` returns null if key not found
- Returns `true` if deleted, `false` if not found
- Controller uses this to return 204 vs 404

**`isValidPair()`:**
```java
private boolean isValidPair(String pair)
```
- Splits pair into from/to (e.g. "USD" and "CAD")
- Lazily fetches supported currencies from XE API on first call
- Caches result in `volatile List<String>` with `synchronized` block
- Checks both currencies exist in the supported list

**`isTriggered()`:**
```java
boolean isTriggered(Alert alert)
```
- Fetches live rate via `RateService.getMidRate()`
- Uses `BigDecimal.compareTo()` — never `==` or `equals()` for financial values
- Switch expression on `Direction` enum — exhaustive, Open/Closed compliant
- Returns `true` only for strict comparison — at threshold = not triggered

**`toResponse()`:**
```java
private AlertResponse toResponse(Alert alert)
```
- Converts internal `Alert` model → `AlertResponse` DTO
- Wraps `isTriggered()` in try/catch — graceful failure sets `evaluationError`
- Single place where triggered is evaluated — no duplication

---

## 37. `RatesController.java` — what was refactored and why?

**Before (original):**
```java
// Same 8 lines repeated 3 times for USD/CAD, GBP/USD, EUR/USD
HttpHeaders headers = new HttpHeaders();
headers.set("Authorization", "Basic " + credentials);
ResponseEntity<String> response = restTemplate.exchange(...);
// parse...
```

**After (refactored):**
```java
@GetMapping
public List<Map<String, Object>> getRates() {
    return List.of(
            fetchRate("USD", "CAD"),
            fetchRate("GBP", "USD"),
            fetchRate("EUR", "USD")
    );
}

private Map<String, Object> fetchRate(String from, String to) {
    return Map.of(
            "pair", from + "/" + to,
            "rate", rateService.getMidRate(from, to)
    );
}
```

**What changed:**
- Extracted all HTTP/parsing logic to `RateService`
- Controller now has zero knowledge of HTTP calls, credentials, or JSON parsing
- `fetchRate()` private helper eliminates remaining duplication
- Added `GET /api/rates/pairs` on feature branch — reuses same `RateService`

**Why this matters:**
- Single Responsibility — controller handles HTTP routing only
- DRY — one place to change if XE API changes
- `AlertService` can reuse `RateService` without duplicating HTTP code

---

## 38. `GlobalExceptionHandler.java` — why does exception order matter?

```java
@ExceptionHandler(IllegalArgumentException.class)  // MORE SPECIFIC — first
public ResponseEntity<...> handleIllegalArgument(IllegalArgumentException ex) { ... }

@ExceptionHandler(RuntimeException.class)  // MORE GENERAL — second
public ResponseEntity<...> handleRuntimeException(RuntimeException ex) { ... }
```

**Why order matters:**
`IllegalArgumentException` extends `RuntimeException`. If `RuntimeException` handler is declared first, Spring uses it for ALL runtime exceptions — including `IllegalArgumentException`. The more specific handler never gets called.

**Rule: always declare more specific exceptions before more general ones.**

**What each handler returns:**

| Exception | Status | When |
|-----------|--------|------|
| `MethodArgumentNotValidException` | 400 | `@Valid` validation fails |
| `IllegalArgumentException` | 400 | Invalid pair, bad argument |
| `RuntimeException` | 500 | Unexpected errors, XE API failures |

**Why `GlobalExceptionHandler` vs try/catch everywhere:**
- One place for all error handling — no duplication across controllers
- Consistent JSON error format for all errors
- Controllers stay thin — no error handling logic

---

## 39. What is `state.ts` and why does it exist?

Think of `state.ts` as a **global variable** that the whole frontend can read and write to. When it changes → Vue automatically updates the UI.

```typescript
export const state = reactive({
  rates: [] as Rate[],
  alerts: [] as Alert[],
  currencies: [] as Currency[],
  lastUpdated: '',
})
```

**Where it's used in `App.vue`:**
- `loadRates()` writes to `state.rates` → rate cards re-render
- `loadAlerts()` writes to `state.alerts` → alert list re-renders
- `loadCurrencies()` writes to `state.currencies` → dropdowns re-render

---

## 40. Vue concepts used in `App.vue` — where are they?

All in `frontend/src/App.vue`:

| Concept | Where |
|---------|-------|
| `v-for` | Rate cards loop, alerts list, currency dropdowns |
| `v-model` | `newFromCurrency`, `newToCurrency`, `newDirection`, `newThreshold` |
| `v-if` | `state.lastUpdated`, `alert.evaluationError`, `state.alerts.length === 0` |
| `ref()` | Form fields — `newFromCurrency`, `newToCurrency`, `newThreshold`, `newDirection` |
| `computed()` | `newPair` — combines from/to into `"USD/CAD"` |
| `onMounted()` | Calls `loadRates()`, `loadAlerts()`, `loadCurrencies()` on page load |
| `:class` | `alert-row` — adds `triggered` or `error` CSS class based on alert state |

**What to say if asked about the frontend:**
> "I kept it minimal — not judging CSS. I focused on wiring the three endpoints correctly and did a few small refactors: replaced three identical rate lookup functions with one `getRate(pair)` and a `v-for` loop. Same DRY principle I applied on the backend with `RateService`."

---

## 41. `App.vue` — short summary of each section

**`<script setup>` — the logic**
- Form fields (`newFromCurrency`, `newToCurrency`, `newThreshold`, `newDirection`) — hold what the user typed
- `newPair` — auto-combines from/to into `"USD/CAD"`
- `loadRates()` — calls `GET /api/rates` → updates `state.rates`
- `loadAlerts()` — calls `GET /api/alerts` → updates `state.alerts`
- `loadCurrencies()` — calls `GET /api/rates/pairs` → populates dropdowns
- `createAlert()` — calls `POST /api/alerts` → reloads alerts
- `deleteAlert()` — asks confirmation → calls `DELETE /api/alerts/{id}`
- `onMounted()` — runs all three load functions when page opens

**`<template>` — the UI**
- Rate cards — loops `state.rates`, shows pair and rate
- Alert form — two currency dropdowns, direction, threshold, Add Alert button
- Alert list — loops `state.alerts`, shows triggered/watching/error status, delete button

**`<style>` — CSS only, ignore it**

---

## 42. How would you scale this app to handle millions of alerts?

**Architecture diagram:**
```
Client/UI
    ↓
Load Balancer
    ↓
Spring Boot Instance 1 | Instance 2 | Instance N  ←── Kafka (RateUpdated topic)
    ↓                                                        ↑
PostgreSQL (alerts)                            Rate Fetcher (@Scheduled, every 60s)
                                                        ↑           ↓
                                                    XE API      Redis Cache
                                               (rate source)  (rate lookup)
```

Current design stores alerts in a `ConcurrentHashMap` in a single JVM — can't scale horizontally because each instance has its own memory.

**Architecture for scale:**

**1. Move to PostgreSQL**
Remove `ConcurrentHashMap` — store alerts in a database. All instances share the same data. `AlertService` swaps `ConcurrentHashMap` for a JPA repository — controller stays untouched.

**2. Stateless backend**
Once state is in the DB, any instance can handle any request. Put a load balancer in front of multiple Spring Boot instances — horizontal scaling becomes trivial.

**3. Cache rates with Redis**
Instead of calling XE API on every alert evaluation:
```java
@Cacheable("rates")
public BigDecimal getMidRate(String pair) {
    // only hits XE API if not in Redis cache
}
```
Add `spring-boot-starter-data-redis` + `@EnableCaching`. Rates cached for 60 seconds — one XE API call per minute instead of one per alert per request.

**4. Evaluate alerts on a schedule via Kafka**
A `@Scheduled` Rate Fetcher job runs every 60 seconds:
- Fetches live rates from XE API
- Caches in Redis
- Publishes `RateUpdated` event to Kafka topic

Each Spring Boot instance consumes from Kafka:
```java
@KafkaListener(topics = "rate-updated")
public void onRateUpdated(RateUpdatedEvent event) {
    // evaluate all alerts against new rate
    // update triggered status in PostgreSQL
}
```
Decouples rate fetching from alert evaluation completely.

**5. The result**
- Multiple stateless instances behind a load balancer
- PostgreSQL for persistence
- Redis for rate caching
- Kafka for event-driven evaluation
- XE API called once per minute, not once per request

This is the same pattern as the Moneris Webhook Platform — stateless services, shared DB, event-driven processing.

---

## 43. What happens if the XE API goes down?

Current behavior — `getMidRate()` throws `RuntimeException`. We already handle it partially in `toResponse()` — sets `evaluationError` instead of crashing the alerts list. But `GET /api/rates` would still return 500.

**Full production solution:**

**1. Circuit Breaker (Resilience4j)**
After N consecutive failures, stop calling XE API for X seconds:
```java
@CircuitBreaker(name = "xeApi", fallbackMethod = "getCachedRate")
public BigDecimal getMidRate(String from, String to) {
    // calls XE API
}

public BigDecimal getCachedRate(String from, String to, Exception e) {
    // return last known rate from Redis
    return redisTemplate.opsForValue().get("rate:" + from + "/" + to);
}
```

**2. Retry with backoff**
Before opening the circuit, retry 3 times with exponential backoff:
```java
@Retry(name = "xeApi", fallbackMethod = "getCachedRate")
```

**3. Result**
- XE API down → circuit opens → fallback to Redis cache
- Alerts still evaluate against last known rate
- UI shows stale rates with a warning instead of crashing
- Circuit resets automatically when XE API recovers

**What you say in the panel:**
> "We already handle evaluation failure gracefully with `evaluationError`. For the rates endpoint I'd add Resilience4j circuit breaker with Redis fallback — serve stale rates rather than 500. At Moneris we use similar patterns for third-party payment processor outages."

---

## 44. How would you handle 10,000 concurrent users?

Current design — synchronous Spring Boot with default Tomcat thread pool (200 threads). At 10,000 concurrent users this blocks immediately.

**1. Scale horizontally (most important)**
Load balancer + multiple stateless instances. 10 instances × 200 threads = 2,000 threads. Combined with connection pooling handles most traffic.

**2. Switch to reactive (Spring WebFlux)**
Replace Spring MVC with Spring WebFlux — non-blocking I/O handles thousands of requests on a small thread pool:
```java
// Instead of:
public List<AlertResponse> listAll() { ... }

// Reactive:
public Flux<AlertResponse> listAll() { ... }
```
Especially useful here because `getMidRate()` is an HTTP call — ideal for non-blocking.

**3. Connection pooling (HikariCP)**
With PostgreSQL, each request needs a DB connection. HikariCP (Spring Boot default) pools connections:
```properties
spring.datasource.hikari.maximum-pool-size=20
```

**4. Rate limiting**
Prevent abuse — one user flooding `GET /api/alerts`:
```java
@RateLimiter(name = "alertsApi")
public List<AlertResponse> listAll() { ... }
```

**What you say in the panel:**
> "First I'd scale horizontally — multiple instances behind a load balancer. For the XE API calls specifically, Spring WebFlux would be a strong fit since they're I/O-bound. At Moneris we handled high throughput on our Webhook Platform with similar patterns — horizontal scaling and async processing."

---

## 45. How would you make alert evaluation real-time instead of on-request?

**Current approach:**
- User opens browser → `GET /api/alerts` → evaluate now → return results
- Problem: if no one opens the app, nothing gets evaluated

---

**With Kafka:**

1. **Rate Fetcher** (producer) — runs every 60s, fetches rate from XE API, publishes `RateUpdated` event to a Kafka topic

2. **Alert Evaluator** (consumer) — listens to that topic, when a new rate arrives it evaluates ALL alerts against it, stores triggered state in PostgreSQL

3. **Frontend** — just reads the stored triggered state from PostgreSQL via `GET /api/alerts` — no evaluation happens at request time anymore

---

**The key difference:**

| Now | With Kafka |
|-----|-----------|
| Evaluation triggered by user request | Evaluation triggered by rate change |
| No request = no evaluation | Evaluates every 60s regardless |
| Rate fetched on every request | Rate fetched once, shared via Kafka |
| Stateless triggered field | Triggered state stored in PostgreSQL |

This is exactly the same pattern as Moneris Webhook Platform — a producer publishes events to a Kafka topic, a processor consumes them and does the work. Same pattern here — just the event is `RateUpdated` instead of a webhook.

**What you say in the panel:**
> "Current approach evaluates on demand — fine for a demo. For production I'd use Kafka — a Rate Fetcher produces `RateUpdated` events, an Alert Evaluator consumer processes them and stores triggered state in PostgreSQL. Frontend just reads the stored state. Same pattern I use at Moneris for webhook processing."

---

## 46. How would you add a database?

Three changes only:

**1. `Alert.java`** — convert record to `@Entity` class with `@Id`, `@GeneratedValue`, and `@Enumerated(EnumType.STRING)` for the Direction enum.

**2. Add `AlertRepository`** — interface extending `JpaRepository<Alert, UUID>`. Spring generates all CRUD operations automatically.

**3. `AlertService`** — swap `ConcurrentHashMap` for the repository. `create()`, `listAll()`, `delete()` all delegate to the repository instead of the map.

Controller stays completely untouched — the abstraction was designed correctly from the start.

Add `spring-boot-starter-data-jpa` and `postgresql` driver to `pom.xml`. Add connection properties to `application.properties`.

**What you say in the panel:**
> "Three changes — `Alert` becomes `@Entity`, add a `JpaRepository`, swap the map in `AlertService`. Controller stays untouched because I designed the abstraction correctly from the start."

---

## 47. Which database would you choose and why?

**PostgreSQL — relational/SQL database.**

Why SQL over NoSQL:
- Alerts have a clear fixed schema — `id`, `pair`, `threshold`, `direction`, `createdAt`
- Queries are simple — find by id, list all, delete by id
- No need for flexible schema or document storage
- ACID transactions matter in a financial context — you don't want partial writes

Why PostgreSQL specifically:
- Industry standard for fintech
- Excellent Spring/JPA support
- Handles millions of alerts easily
- XE is a financial company — they'd expect a proper relational DB

**What you say in the panel:**
> "PostgreSQL. Alerts have a fixed schema and we need ACID guarantees — relational is the right fit. NoSQL would be over-engineering for this use case. In a financial context like XE, data integrity matters more than schema flexibility."

---

## 48. What's missing before this goes to production?

**Security:**
- No authentication — anyone can create/delete alerts
- No HTTPS — credentials sent in plain text
- API key in `application.properties` — should be in a secrets manager (Azure Key Vault, AWS Secrets Manager)
- No rate limiting — anyone can flood the API

**Reliability:**
- In-memory storage — alerts lost on restart
- No circuit breaker — XE API down = app down
- No retry logic on XE API failures
- No health check endpoint (`/actuator/health`)

**Observability:**
- No metrics (Prometheus/Grafana)
- No distributed tracing (Zipkin/Jaeger)
- Logs are local only — no centralized logging (ELK stack)
- No alerting on errors

**Operations:**
- No Docker image — can't deploy to Kubernetes
- No CI/CD pipeline
- No environment-specific configs (dev/staging/prod profiles)
- No database migrations (Flyway/Liquibase)

**What you say in the panel:**
> "Security first — auth, HTTPS, secrets management. Then reliability — persistent storage, circuit breaker. Then observability — metrics, tracing, centralized logs. At Moneris all of these are non-negotiable before anything goes to production."

---

## 49. The currency cache never expires — what's the problem and how do you fix it?

**The problem:**
In `AlertService.isValidPair()` the supported currencies are cached in memory on first call and never refreshed:

- XE adds a new currency → our cache doesn't know about it → users can't create alerts for it
- XE removes a currency → our cache still allows it → users create alerts that will always fail
- App runs for months → cache becomes increasingly stale
- Only way to refresh is to restart the server

**How to fix it:**

**Option 1 — TTL on the cache (simplest)**
Add a timestamp to the cache. If older than 24 hours → re-fetch from XE API. Thread-safe with same `synchronized` block.

**Option 2 — Spring `@Cacheable` with Redis TTL**
Use Spring Cache with Redis — set TTL of 24 hours. Redis automatically evicts the cache after that. Next call re-fetches from XE API automatically.

**Option 3 — `@Scheduled` refresh**
Background job refreshes the cache every 24 hours regardless of requests.

**What you say in the panel:**
> "The cache never expires — stale currencies are the risk. Simplest fix is adding a timestamp and re-fetching if older than 24 hours. Production fix would be Spring Cache with Redis TTL — automatic expiry, no manual timestamp management."

---

## 50. How would you add authentication?

**The simplest approach — JWT (JSON Web Token):**

**How it works:**
1. User logs in with username/password → server returns a JWT token
2. User includes token in every request header: `Authorization: Bearer <token>`
3. Spring Security validates the token on every request
4. Invalid/missing token → 401 Unauthorized

**What to add:**
- `spring-boot-starter-security` dependency
- JWT library (jjwt)
- `SecurityConfig` — defines which endpoints are public vs protected
- `JwtFilter` — validates token on every request
- `AuthController` — `POST /api/auth/login` endpoint

**Endpoint protection:**
- `POST /api/auth/login` → public
- `GET /api/rates` → public (anyone can see rates)
- `GET/POST/DELETE /api/alerts` → protected (must be logged in)

**Multi-user consideration:**
Each alert would need a `userId` field — users only see their own alerts. `GET /api/alerts` filters by the authenticated user's ID extracted from the JWT token.

**Production alternative:**
Delegate to an identity provider — Auth0, Azure AD, Keycloak. Don't build auth yourself in production.

**What you say in the panel:**
> "Add Spring Security with JWT. Public endpoints for rates, protected endpoints for alerts. Each alert gets a userId — users only see their own alerts. In production I'd use an identity provider like Azure AD rather than building auth from scratch — same approach we use at Moneris."

---

## 51. How would you monitor it?

**Three pillars of observability:**

**1. Metrics (what's happening now)**
- Add `spring-boot-starter-actuator` + Micrometer
- Exposes `/actuator/health`, `/actuator/metrics`
- Connect to Prometheus → visualize in Grafana
- Key metrics to track:
   - Number of alerts created/deleted per minute
   - XE API response time and error rate
   - JVM memory and thread count
   - HTTP request rate and latency per endpoint

**2. Logs (what happened)**
- Already have slf4j logging in place
- In production — ship logs to ELK stack (Elasticsearch, Logstash, Kibana) or Splunk
- Add correlation ID to every request — trace a single request across all log lines
- Alert on ERROR log spike

**Alerts to set up:**
- XE API error rate > 5% → page on-call
- Alert evaluation taking > 2s → investigate
- JVM memory > 80% → scale up
- 0 alerts created in last hour → possible frontend issue

**What you say in the panel:**
> "Two pillars — metrics with Prometheus/Grafana, centralized logs with ELK. We already have structured logging in place which is the foundation. At Moneris we use a similar observability stack for the Webhook Platform."

---

## 52. What happens to alerts when the server restarts?

**Current behavior:**
All alerts are lost. The `ConcurrentHashMap` lives in JVM memory — when the server restarts, memory is wiped. Users have to recreate all their alerts.

**Why this is acceptable for now:**
- Assignment explicitly says in-memory is fine
- Documented in NOTES.md as a known trade-off
- It's a demo — not production

**What you say in the panel:**
> "All alerts are lost on restart — that's the trade-off of in-memory storage. I documented this explicitly in NOTES.md. The fix is PostgreSQL — alerts persist across restarts. The `AlertService` abstraction means swapping storage is a one-file change."

---

## 53. How would you add multi-user support?

**Current problem:**
All alerts are shared — every user sees everyone else's alerts. No concept of ownership.

**Three changes needed:**

**1. Add `userId` to `Alert`**
Each alert belongs to a user. When created, store the authenticated user's ID on the alert.

**2. Filter alerts by user**
`GET /api/alerts` only returns alerts belonging to the authenticated user — not everyone's alerts. Repository query filters by `userId` from the JWT token.

**3. Authorization on delete**
Before deleting — verify the alert belongs to the requesting user. Prevent user A from deleting user B's alerts. Return 403 Forbidden if not the owner.

**What you say in the panel:**
> "Add `userId` to the `Alert` model, extracted from the JWT token on every request. `listAll()` filters by userId, `delete()` checks ownership before removing. Three changes — model, repository query, and an ownership check in the service."

---

## 54. What if `GET /api/alerts` is called 1000 times per second?

**Current problem:**
Every call to `GET /api/alerts` calls `getMidRate()` for every alert. 10 alerts × 1000 requests/second = 10,000 XE API calls per second. XE will rate limit us immediately.

We can see the rate limit in the API response headers:
- `x-ratelimit-limit: 900` — only 900 calls per hour
- 1000 calls per second would exhaust that in milliseconds

**Solutions:**

**1. Rate caching (immediate fix)**
Cache the rate in Redis for 60 seconds. 1000 requests/second all hit Redis — only 1 call to XE API per minute.

**2. Rate limiting on our API**
Limit each client to X requests per minute using Resilience4j `@RateLimiter` or an API gateway. Excess requests get 429 Too Many Requests.

**What you say in the panel:**
> "Current design calls XE API on every request — that hits rate limits immediately. Short term fix is Redis caching. We also need rate limiting on our own API to prevent abuse — 429 Too Many Requests for clients that exceed the limit."

---

## 55. How would you reduce XE API calls?

**Current problem:**
Every `GET /api/alerts` call fetches a live rate for each alert. 5 alerts = 5 XE API calls per request. Expensive, slow, and hits rate limits fast.

**Solutions in order of priority:**

**1. Redis cache with TTL (most impactful)**
Fetch rate once, cache in Redis for 60 seconds. All requests within that minute share the same cached rate. 1000 requests = 1 XE API call per minute instead of 1000.

**2. Batch rate fetch**
Instead of calling XE API once per pair per alert — fetch all needed pairs in one call. XE API supports multiple currencies in one request using comma-separated `to` parameter:
- `/convert_from.json?from=USD&to=CAD,GBP,EUR`
- One call for all pairs instead of one per pair.

**3. Background scheduler**
Fetch rates on a schedule (every 60s) instead of on request. Store in Redis. `GET /api/alerts` never calls XE API directly.

**What you say in the panel:**
> "Two quick wins — Redis caching eliminates duplicate calls within 60 seconds, and batching fetches all pairs in one XE API call instead of one per pair. Combined, these reduce XE API calls from thousands per minute to just a handful."

---