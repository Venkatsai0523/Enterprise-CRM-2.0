# Enterprise CRM 2.0

A modular-monolith CRM backend built to demonstrate production-grade Spring Boot architecture: event-driven domain design, lead scoring, and clean layering — not a CRUD tutorial clone.

> **Status:** In active development. See [Scope](#scope) for what's implemented vs. designed-for-later.

---

## Why this project exists

Most portfolio CRMs are a `Contact` entity with REST endpoints. This one models the actual sales lifecycle — lead → qualification → opportunity → won/lost → customer account — and uses that lifecycle to justify real architectural decisions: where events matter, where consistency matters, and where a rules engine vs. a service class is the right tool.

Each major decision below is deliberate and explained, because the goal is to show *engineering judgment*, not just framework familiarity.

---

## Business Process

```
Website / Mobile App / Manual Entry
             │
             ▼
      Lead Management
             │
   AI Lead Scoring Engine
             │
             ▼
  Auto Assignment Engine
             │
             ▼
     Sales Representative
             │
   Calls • Emails • Meetings
             │
             ▼
    Opportunity Pipeline
             │
  Proposal • Negotiation
             │
      ┌──────┴──────┐
      ▼             ▼
   Won Deal      Lost Deal
      │             │
      ▼             ▼
Customer Account  Lost Analysis
      │
      ▼
Support • Renewals • Upselling
```

---

## High-Level Architecture

```
                    Web (React)
                         │
                    REST API
                         │
                Spring Boot Backend
                         │
 ┌───────────┬───────────┬───────────┐
 │           │           │           │
Identity  Lead Domain  Customer   Sales Domain
 │           │        Domain        │
 └───────────┴───────────┴───────────┘
                         │
             Workflow & Event Engine
                         │
    Notifications • Audit Logs • Reports
                         │
                 PostgreSQL Database
```

**Why a modular monolith, not microservices:** at this scale, network calls between services add latency and operational overhead without a corresponding benefit — there's one team (me) and one deployable unit. The domains are still isolated at the package level with enforced boundaries (see [Module Boundaries](#module-boundaries)), so a future split into services is possible without a rewrite, but isn't paid for up front.

---

## Package Structure

```
crm
├── identity        # Auth, users, roles, permissions
├── organization     # Multi-org/tenant scaffolding
├── lead             # Lead intake, scoring, assignment
├── customer         # Customer 360, accounts
├── opportunity      # Pipeline, proposals, won/lost
├── task             # Activities, calendar
├── workflow         # Rules engine / event orchestration
├── notification     # Email, in-app, webhook notifications
├── analytics        # Reporting, dashboards
├── audit            # Change history, compliance logging
├── common           # Shared DTOs, exceptions, utils
└── infrastructure    # Config, messaging, storage adapters
```

### Module Boundaries

Cross-domain calls only happen through published interfaces (`*.api` sub-packages) or async events — never by reaching into another domain's repository or entity directly. This is enforced with **ArchUnit** tests in CI, so a package boundary violation fails the build instead of relying on code review discipline.

---

## Scope

Building all twelve domains to production depth isn't realistic for a single-developer project, so scope is split deliberately:

### v1 — Built & Verified

| Domain | What it demonstrates |
|---|---|
| Identity & Access | Spring Security, JWT, role-based access control |
| Lead Intelligence | Event-driven scoring + auto-assignment — the centerpiece feature |
| Opportunity Pipeline | Stateful business process modeling, won/lost transitions |
| Customer 360 | Data modeling across lead→opportunity→customer conversion, dedup logic |
| Notification + Audit | Real-time Kafka notification consumer + compliance audit logging |
| Quality & Hardening | ArchUnit package boundary rules + JaCoCo >= 80% line coverage gate |

### Designed for, not yet built

| Domain | Why it's deferred |
|---|---|
| Workflow Engine (rules engine) | Real scope — a rules engine (Drools-style) deserves its own design pass rather than being rushed |
| Integration Hub | External integrations (email providers, calendars) add breadth, not depth, to the core demo |
| AI Assistant (chat) | Separate product surface from the core pipeline; lower priority than lead scoring |
| Multi-tenancy | Architectural fork (row-level security vs. schema-per-tenant) — noted in design docs, implemented only if time allows |

Being explicit about this split is intentional: it's a more honest signal of engineering maturity than quietly stopping at whatever got finished.

---

## Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Framework | Spring Boot | Industry-standard, demonstrates ecosystem fluency |
| Security | Spring Security + JWT | Table-stakes for any backend role |
| Database | PostgreSQL | Relational integrity for the lead→opportunity→customer lifecycle |
| Migrations | Flyway | Versioned schema, no manual DB drift |
| Mapping | MapStruct | Compile-time DTO↔entity mapping, no reflection overhead |
| Cache | Redis | Session storage + hot-path caching (lead scores, dashboard aggregates) |
| Eventing | Kafka | Decouples lead scoring/assignment from the request path |
| Search | Elasticsearch | *Planned for v2* — added once core write-path is solid, since it introduces a sync problem (see below) |
| Containerization | Docker / Docker Compose | One-command local environment |
| Docs | OpenAPI/Swagger | Explorable API without a frontend |
| CI | GitHub Actions | Tests + build gate on every PR |
| Observability | Prometheus + Grafana | Basic metrics, shows ops-awareness |

**Note on Elasticsearch:** it's listed in the original design but deliberately sequenced *after* core CRUD and business logic. Elasticsearch as a read model requires a sync strategy against Postgres (outbox pattern via Kafka, or CDC) — bolting it on early would mean solving a hard consistency problem before the domain logic it's meant to search even exists.

---

## Key Design Decisions

**1. Lead scoring is synchronous, assignment is async.**
Scoring needs to return a result immediately so the UI can show it on lead creation. Auto-assignment, however, is published as an event — it can tolerate a few hundred ms of delay, and decoupling it means the assignment logic (which will get more complex — territory rules, rep capacity, etc.) can evolve independently of lead intake.

**2. Lead-to-customer conversion goes through an explicit dedup check.**
Rather than blindly creating a new customer record when a deal is won, conversion queries existing customer records by normalized email/domain first. This avoids the classic CRM failure mode of duplicate contact records.

**3. Outbox pattern for Elasticsearch sync (v2).**
Dual-writing to Postgres and Elasticsearch in the same request risks drift if one write fails. The plan is an outbox table + Kafka consumer to update the search index asynchronously, with eventual consistency as an explicit, documented tradeoff.

---

## Getting Started

```bash
docker-compose up -d          # Postgres, Redis, Kafka
./mvnw.cmd test               # Run all test suites
./mvnw.cmd verify             # Run build, ArchUnit checks & JaCoCo coverage gate
./mvnw.cmd spring-boot:run   # Start Spring Boot application
```

API docs available at `http://localhost:8080/swagger-ui.html` once running.

---

## Roadmap

- [x] Core domains (Identity, Lead, Opportunity, Customer, Notification, Audit)
- [x] Event-driven lead scoring + auto-assignment
- [x] Test suite (25 test cases: unit + integration + ArchUnit + JaCoCo >= 80% coverage gate)
- [x] CI pipeline (GitHub Actions)
- [ ] Elasticsearch + outbox sync
- [ ] Workflow/rules engine
- [ ] Multi-tenancy design doc

Enterprise CRM 2.0 — API Reference

Quick-scan reference for every endpoint, followed by a plain-English walkthrough of the full sales lifecycle.

Quick Reference Table
Module	Method	Endpoint	Who can call it	What it does
Auth	POST	/api/auth/register	Public	Create a user
Auth	POST	/api/auth/login	Public	Get JWT + refresh token
Auth	POST	/api/auth/refresh	Public	Get a new JWT
Leads	POST	/api/leads	Admin, Rep, Manager	Create + score a lead
Leads	GET	/api/leads/{id}	Admin, Rep, Manager	View one lead
Leads	GET	/api/leads	Admin, Rep, Manager	List/filter leads
Opportunities	POST	/api/opportunities	Admin, Rep, Manager	Create an opportunity
Opportunities	PATCH	/api/opportunities/{id}/stage	Admin, Rep, Manager	Move deal to next stage
Opportunities	GET	/api/opportunities/{id}	Admin, Rep, Manager	View one opportunity
Opportunities	GET	/api/opportunities	Admin, Rep, Manager	List/filter opportunities
Opportunities	GET	/api/opportunities/analytics/lost-analysis	Admin, Manager	Lost-deal breakdown
Customers	GET	/api/customers	Admin, Rep, Manager	List customer accounts
Customers	GET	/api/customers/{id}	Admin, Rep, Manager	Full 360° customer view
Notifications	GET	/api/notifications	Admin, Rep, Manager	List notifications
Notifications	PATCH	/api/notifications/{id}/read	Admin, Rep, Manager	Mark as read
Audit	GET	/api/audit-logs	Admin, Manager	Query the compliance trail
The Sales Lifecycle, in Order
Lead created ──► Scored (instant) ──► Auto-assigned to a rep (background)
                                              │
                                              ▼
                                    Opportunity created
                                              │
                          Proposal ──► Negotiation ──► Won / Lost
                                              │
                               ┌──────────────┴──────────────┐
                               ▼                              ▼
                     Deal WON: customer account       Deal LOST: reason
                     created or matched (background)   logged for analysis
Module Details
🔐 Auth — /api/auth

Register a user

POST /api/auth/register
json
{
  "email": "jane@nexus.com",
  "password": "password123",
  "firstName": "Jane",
  "lastName": "Representative",
  "roleName": "ROLE_SALES_REP"
}

→ Returns the created user (no password in the response).

Log in

POST /api/auth/login
json
{ "email": "jane@nexus.com", "password": "password123" }

→ Returns a JWT token (use this as your Bearer token) plus a refreshToken.

Refresh a token

POST /api/auth/refresh
json
{ "refreshToken": "7c13ab01-..." }

→ Returns a new access + refresh token pair.

📥 Leads — /api/leads

Create a lead — scoring happens instantly; assignment happens moments later in the background.

POST /api/leads
json
{
  "firstName": "Bruce",
  "lastName": "Wayne",
  "email": "bruce@waynecorp.com",
  "phone": "+1-555-0100",
  "companyName": "Wayne Enterprises",
  "companySize": ">500",
  "leadSource": "WEBSITE"
}

→ Response includes a score (0–100) right away. status starts at NEW, then flips to ASSIGNED once a rep is auto-picked in the background.

Get one lead

GET /api/leads/{id}

List leads

GET /api/leads?status=ASSIGNED&minScore=70&maxScore=100&page=0&size=10
Filter	Values
status	NEW, ASSIGNED, CONTACTED, QUALIFIED, LOST, CONVERTED
minScore / maxScore	0–100
💼 Opportunities — /api/opportunities

Create an opportunity — always starts at PROSPECTING.

POST /api/opportunities
json
{
  "title": "Wayne Enterprises Batmobile Fleet",
  "leadId": "e0000000-...",
  "estimatedValue": 250000.00
}

Move a deal forward — call this once per stage change.

PATCH /api/opportunities/{id}/stage

Stages, in order: PROSPECTING → PROPOSAL → NEGOTIATION → WON | LOST

Winning:

json
{ "stage": "WON" }

Losing (reason required):

json
{ "stage": "LOST", "lostReason": "Competitor offered a lower price" }

Reaching WON or LOST fires a background event — that's what triggers customer-account creation or the lost-deal log.

Get one opportunity

GET /api/opportunities/{id}

List opportunities

GET /api/opportunities?stage=NEGOTIATION&leadId=...&page=0&size=10

Lost-deal breakdown (Admin/Manager only)

GET /api/opportunities/analytics/lost-analysis

→ Returns counts and total value lost, grouped by reason:

json
[{ "lostReason": "Competitor offered a lower price", "count": 2, "totalValue": 75000.00 }]
👤 Customers — /api/customers

List customer accounts

GET /api/customers

Full customer profile — includes every linked opportunity and total lifetime value.

GET /api/customers/{id}
json
{
  "accountName": "Wayne Enterprises",
  "domainName": "waynecorp.com",
  "status": "ACTIVE",
  "opportunityCount": 1,
  "totalLifetimeValue": 250000.00,
  "opportunities": [ { "title": "...", "stage": "WON", "closedAt": "..." } ]
}
🔔 Notifications — /api/notifications

List notifications for a rep

GET /api/notifications?recipientId={uuid}&page=0&size=10

Mark one as read

PATCH /api/notifications/{id}/read
📜 Audit Logs — /api/audit-logs (Admin/Manager only)
GET /api/audit-logs?entityName=LEAD&entityId={uuid}&action=STATUS_CHANGE
Filter	Values
entityName	LEAD, OPPORTUNITY
action	e.g. STAGE_CHANGE, STATUS_CHANGE
Full Walkthrough: One Lead, Start to Finish

1. A lead comes in and gets scored instantly. POST /api/leads with Clark Kent's info → score comes back as 85 in the same response. Behind the scenes, a lead.scored event goes onto the queue.

2. A background process assigns the lead to a rep. No API call needed — a listener picks up the lead.scored event, assigns the lead to the next rep in rotation, writes an audit entry (status: NEW → ASSIGNED), and sends that rep a notification.

3. The rep converts the lead into an opportunity. POST /api/opportunities referencing the lead ID → opportunity starts at PROSPECTING.

4. The rep moves the deal through the pipeline. Three separate PATCH /api/opportunities/{id}/stage calls: → PROPOSAL → NEGOTIATION → WON. Hitting WON fires a deal-won event.

5. A background process creates or matches the customer account. A listener catches the deal-won event, checks whether a customer account already exists for that email domain (dailyplanet.com), and either creates a new account or attaches the opportunity to the existing one. The original lead's status flips to CONVERTED.

6. Anyone with access can pull the full customer picture. GET /api/customers/{id} → one response with the account, every linked opportunity, and lifetime value.

The key thing to notice: steps 1, 3, 4, and 6 are things a user (or your frontend) triggers directly. Steps 2 and 5 happen automatically in the background — that's the event-driven part of the architecture doing its job.
