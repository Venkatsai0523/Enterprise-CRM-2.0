# Enterprise CRM 2.0 — Agentic Build Spec

**Purpose of this document:** This is a milestone-ordered brief for an agentic coding tool (Google Antigravity, Claude Code, etc.) to build this project phase by phase. Do not attempt all milestones in a single unsupervised run — complete one milestone, let the human review, then proceed to the next. Each milestone lists explicit acceptance criteria so the agent (and the human reviewing it) can verify "done" objectively.

Architectural context and rationale live in `README.md` — read that first. This document is the execution plan; the README is the "why."

---

## Non-negotiable constraints (apply to every milestone)

- **Language/Framework:** Java 25, Spring Boot 4.x
- **Build tool:** Maven
- **Package root:** `com.crm` (adjust to your actual group id), matching the structure in README.md (`identity`, `lead`, `customer`, `opportunity`, `task`, `workflow`, `notification`, `analytics`, `audit`, `common`, `infrastructure`)
- **Module boundary rule:** No domain package may import another domain's `.entity`, `.repository`, or `.service` classes directly. Cross-domain interaction happens only via a domain's `.api` sub-package (interfaces + DTOs) or async events. Add an ArchUnit test enforcing this in Milestone 1 and never disable it.
- **No entity leakage:** Controllers never accept or return JPA entities directly — always DTOs, mapped via MapStruct.
- **Every milestone must include tests.** A milestone isn't done when it compiles; it's done when its tests pass, including at least one integration test using Testcontainers where a database or message broker is involved.
- **Every milestone must build in Docker.** `docker-compose up` must bring up all infra dependencies needed so far.
- **Commit discipline:** one commit per logical unit of work within a milestone, not one giant commit at the end. This matters for the finished repo's commit history looking like real engineering, not a single dump.

---

## Milestone 0 — Project Skeleton & Infrastructure

**Goal:** Empty but runnable Spring Boot app with all infra wired up, before any business logic exists.

**Tasks:**
1. Generate Spring Boot project (Web, Data JPA, Security, Validation, Actuator starters)
2. Set up package structure per README (`crm.identity`, `crm.lead`, etc. — empty packages with `package-info.java` placeholders)
3. `docker-compose.yml` with: PostgreSQL, Redis, Kafka (with Zookeeper or KRaft mode), and a health-check for each
4. Flyway configured, pointing at an empty baseline migration
5. `application.yml` with profiles: `local`, `test`, `docker`
6. GitHub Actions workflow: build + run tests on every push/PR
7. Add ArchUnit dependency and one placeholder boundary test (fails loudly if a domain package doesn't exist yet — replace with real rules in Milestone 1)
8. OpenAPI/Swagger dependency wired up, reachable at `/swagger-ui.html`
9. Prometheus/Actuator endpoints exposed at `/actuator/prometheus`

**Acceptance criteria:**
- `docker-compose up -d && ./mvnw spring-boot:run` starts cleanly with no errors
- `/actuator/health` returns UP
- `/swagger-ui.html` loads (even with zero documented endpoints)
- CI pipeline passes on an empty commit

---

## Milestone 1 — Identity & Access

**Goal:** Working auth: register, login, JWT issuance, role-based endpoint protection.

**Tasks:**
1. Entities: `User`, `Role`, `Permission` (many-to-many User↔Role, Role↔Permission)
2. Flyway migration for identity tables
3. `POST /api/auth/register`, `POST /api/auth/login` (returns JWT), `POST /api/auth/refresh`
4. Spring Security filter chain: JWT validation filter, stateless sessions
5. `@PreAuthorize` examples on at least one protected test endpoint per role
6. Password hashing via BCrypt
7. DTOs + MapStruct mappers (`UserRegistrationDto`, `UserResponseDto` — never expose password hash)
8. Real ArchUnit boundary tests: no other domain may import `crm.identity.entity.*` directly — only `crm.identity.api.*`
9. Global exception handler (`@ControllerAdvice`) — consistent error response shape for the whole app, established here since every later domain will reuse it

**Acceptance criteria:**
- Unit tests for auth service (mocked repository)
- Integration test (Testcontainers Postgres) covering register → login → access protected endpoint → rejected without token
- Swagger shows all auth endpoints with example payloads
- A bad JWT or expired JWT returns 401 with the standard error shape, not a stack trace

---

## Milestone 2 — Lead Intelligence (the centerpiece feature)

**Goal:** Lead intake, scoring, and event-driven auto-assignment. This is the module to spend the most design effort on — it's what differentiates the project.

**Tasks:**
1. Entity: `Lead` (source, contact info, status enum: NEW → SCORED → ASSIGNED → CONVERTED/LOST)
2. `POST /api/leads` — lead intake endpoint (from web/mobile/manual per the business process diagram)
3. **Scoring service** — synchronous, runs on intake, returns a score 0–100 based on a rule set (define a simple weighted rule set: source quality, company size field, engagement signals — document the rules in code comments, this is a talking point for interviews)
4. On scoring completion, publish a `LeadScoredEvent` to Kafka
5. **Assignment consumer** — separate component listening for `LeadScoredEvent`, applies assignment logic (round-robin or territory-based — pick one, document why), publishes `LeadAssignedEvent`
6. Idempotent consumer design: assignment consumer must be safe to receive the same event twice (e.g., check current lead status before acting)
7. `GET /api/leads/{id}`, `GET /api/leads` (paginated, filterable by status/score range)
8. Lead → Rep assignment stored and queryable

**Acceptance criteria:**
- Integration test: POST a lead → assert score is computed synchronously in the response → assert (via polling or test hook) that assignment happens asynchronously afterward
- Test that duplicate delivery of `LeadScoredEvent` does not double-assign
- ArchUnit: lead domain does not reach into identity's internals to resolve "which rep" — goes through identity's `.api` package only

---

## Milestone 3 — Opportunity Pipeline

**Goal:** Model the proposal → negotiation → won/lost state machine.

**Tasks:**
1. Entity: `Opportunity` (linked to `Lead`, stage enum: PROSPECTING → PROPOSAL → NEGOTIATION → WON/LOST)
2. State transition validation — invalid transitions (e.g., WON → PROSPECTING) must be rejected at the service layer, not just the UI
3. `POST /api/opportunities`, `PATCH /api/opportunities/{id}/stage`
4. On WON: publish `DealWonEvent`. On LOST: publish `DealLostEvent` + capture a `lostReason` field (feeds "Lost Analysis" from the business process diagram)
5. Basic `LostAnalysis` read model — simple aggregation endpoint (count/group by lost reason) to demonstrate the analytics angle without building the full analytics domain yet

**Acceptance criteria:**
- Unit tests covering every valid and invalid state transition
- Integration test: win a deal → assert `DealWonEvent` is published → assert customer domain (Milestone 4) reacts to it

---

## Milestone 4 — Customer 360 & Conversion

**Goal:** Handle the won-deal → customer account conversion, including the dedup logic called out in the README.

**Tasks:**
1. Entity: `CustomerAccount`
2. Kafka consumer on `DealWonEvent`: before creating a new `CustomerAccount`, query existing accounts by normalized email domain
3. If match found: link the new opportunity to the existing account instead of duplicating
4. If no match: create new account, carry over relevant lead/opportunity data
5. `GET /api/customers/{id}` — full 360 view: account info + linked opportunities + activity history (activity history can be a stub until Milestone 5's Task domain exists)

**Acceptance criteria:**
- Integration test: two won deals with the same email domain → assert only one `CustomerAccount` is created, both opportunities linked to it
- Integration test: won deal with a new domain → new account created correctly

---

## Milestone 5 — Cross-cutting: Notifications & Audit

**Goal:** Fold these into a shared event-consumer module (per README's scoped-down v1), not full standalone domains.

**Tasks:**
1. Generic `AuditEvent` listener subscribing to all domain events (`LeadScoredEvent`, `DealWonEvent`, etc.) — writes to an `audit_log` table with actor, action, entity, timestamp
2. Simple `NotificationService` — logs/stores a notification record on key events (lead assigned, deal won) — actual email/webhook delivery can be a stub/log statement for v1, per the "Integration Hub deferred" note in the README
3. `GET /api/audit?entityType=&entityId=` — queryable audit trail

**Acceptance criteria:**
- Every event published in Milestones 2–4 has a corresponding audit log row after the fact (verify via integration test)
- Audit writing failure must not fail the originating transaction (fire-and-forget, logged if it fails)

---

## Milestone 6 — Testing Hardening & CI Gate

**Goal:** This milestone exists specifically to avoid the common failure mode of "tests exist but nobody enforces them."

**Tasks:**
1. Add code coverage reporting (JaCoCo)
2. Set a minimum coverage gate in CI (suggest 70% for service-layer classes — don't chase 100%, diminishing returns)
3. Testcontainers-based full integration test: lead intake → score → assign → convert to opportunity → win → customer created → audit trail complete, all in one end-to-end test
4. README updated: remove "planned" language from anything now implemented, keep it honest for anything still deferred

**Acceptance criteria:**
- CI fails if coverage drops below threshold
- The end-to-end integration test passes reliably (not flaky — if it's flaky, it's almost certainly a race condition in the async assignment/conversion flow; fix the root cause, don't add sleep() calls)

---

## Explicitly out of scope for this build spec

Per README: Workflow/rules engine, Integration Hub, AI Assistant chat surface, multi-tenancy, Elasticsearch sync. Do not let the agent "helpfully" start implementing these mid-milestone — if it proposes doing so, redirect it back to the current milestone's acceptance criteria.

---

## How to use this with Antigravity

1. Start a new workspace pointed at this repo (or an empty one where Milestone 0 hasn't run yet)
2. Paste Milestone 0 in full as the task brief, let it plan, review the plan before it executes
3. After each milestone's acceptance criteria are met and you've reviewed the diff, move to the next milestone as a fresh task — don't chain them into one giant autonomous run
4. If the agent's plan diverges from a milestone's stated tasks, stop and correct it before it writes code — cheaper to fix a plan than a finished implementation
