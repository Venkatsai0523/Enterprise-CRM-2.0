# Enterprise CRM 2.0 - API Specification & End-to-End Workflow

This document provides a comprehensive, module-wise and HTTP method-wise reference for all REST endpoints in the system, followed by a step-by-step end-to-end sales lifecycle workflow.

---

## 📁 1. Module-Wise Endpoint Reference

### 🔐 1.1 Identity & Access Module (`/api/auth`)

#### `POST` /api/auth/register
*   **Description:** Registers a new user with BCrypt password hashing.
*   **Role Access:** Public (No authentication required)
*   **Request Headers:** `Content-Type: application/json`
*   **Request Body:**
    ```json
    {
      "email": "salesrep_jane@nexus.com",
      "password": "password123",
      "firstName": "Jane",
      "lastName": "Representative",
      "roleName": "ROLE_SALES_REP"
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "id": "c0a80102-89ab-4cde-b001-c00000000005",
      "email": "salesrep_jane@nexus.com",
      "firstName": "Jane",
      "lastName": "Representative",
      "roles": ["ROLE_SALES_REP"]
    }
    ```

#### `POST` /api/auth/login
*   **Description:** Authenticates credentials and returns JWT access + refresh tokens.
*   **Role Access:** Public
*   **Request Body:**
    ```json
    {
      "email": "salesrep_jane@nexus.com",
      "password": "password123"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "7c13ab01-23cd-45ef-6789-0123456789ab",
      "email": "salesrep_jane@nexus.com",
      "roles": ["ROLE_SALES_REP"]
    }
    ```

#### `POST` /api/auth/refresh
*   **Description:** Generates new JWT access and refresh tokens using a valid refresh token.
*   **Role Access:** Public
*   **Request Body:**
    ```json
    {
      "refreshToken": "7c13ab01-23cd-45ef-6789-0123456789ab"
    }
    ```

---

### 📥 1.2 Lead Intelligence Module (`/api/leads`)

#### `POST` /api/leads
*   **Description:** Registers a new lead, calculates a 0-100 score synchronously, and publishes a `lead.scored` event to Kafka.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Request Body:**
    ```json
    {
      "firstName": "Bruce",
      "lastName": "Wayne",
      "email": "bruce@waynecorp.com",
      "phone": "+1-555-0100",
      "companyName": "Wayne Enterprises",
      "companySize": ">500",
      "leadSource": "WEBSITE"
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "id": "e0000000-0000-0000-0000-000000000003",
      "firstName": "Bruce",
      "lastName": "Wayne",
      "email": "bruce@waynecorp.com",
      "phone": "+1-555-0100",
      "companyName": "Wayne Enterprises",
      "companySize": ">500",
      "leadSource": "WEBSITE",
      "status": "ASSIGNED",
      "score": 95,
      "assignedRepId": "c0000000-0000-0000-0000-000000000002"
    }
    ```

#### `GET` /api/leads/{id}
*   **Description:** Retrieves lead details by ID.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Response (200 OK):**
    ```json
    {
      "id": "e0000000-0000-0000-0000-000000000003",
      "firstName": "Bruce",
      "lastName": "Wayne",
      "email": "bruce@waynecorp.com",
      "phone": "+1-555-0100",
      "companyName": "Wayne Enterprises",
      "companySize": ">500",
      "leadSource": "WEBSITE",
      "status": "ASSIGNED",
      "score": 95,
      "assignedRepId": "c0000000-0000-0000-0000-000000000002"
    }
    ```

#### `GET` /api/leads
*   **Description:** Returns a paginated list of leads filtered by status and score range.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Query Parameters:**
    *   `status`: (Optional) `NEW`, `ASSIGNED`, `CONTACTED`, `QUALIFIED`, `LOST`, `CONVERTED`
    *   `minScore`: (Optional) Integer
    *   `maxScore`: (Optional) Integer
    *   `page`: (Optional, default `0`)
    *   `size`: (Optional, default `10`)

---

### 💼 1.3 Opportunity Pipeline Module (`/api/opportunities`)

#### `POST` /api/opportunities
*   **Description:** Creates a new opportunity linked to a qualified lead, starting in the `PROSPECTING` stage.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Request Body:**
    ```json
    {
      "title": "Wayne Enterprises Batmobile Fleet",
      "leadId": "e0000000-0000-0000-0000-000000000003",
      "estimatedValue": 250000.00
    }
    ```
*   **Response (201 Created):**
    ```json
    {
      "id": "f0000000-0000-0000-0000-000000000005",
      "title": "Wayne Enterprises Batmobile Fleet",
      "leadId": "e0000000-0000-0000-0000-000000000003",
      "estimatedValue": 250000.00,
      "stage": "PROSPECTING",
      "lostReason": null,
      "closedAt": null
    }
    ```

#### `PATCH` /api/opportunities/{id}/stage
*   **Description:** Transitions the stage of an opportunity. Triggers won/lost Kafka events upon reaching a terminal stage.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Request Body (Won):**
    ```json
    {
      "stage": "WON"
    }
    ```
*   **Request Body (Lost):**
    ```json
    {
      "stage": "LOST",
      "lostReason": "Competitor offered a lower price"
    }
    ```
*   **Response (200 OK):**
    ```json
    {
      "id": "f0000000-0000-0000-0000-000000000005",
      "title": "Wayne Enterprises Batmobile Fleet",
      "leadId": "e0000000-0000-0000-0000-000000000003",
      "estimatedValue": 250000.00,
      "stage": "WON",
      "lostReason": null,
      "closedAt": "2026-07-21T23:30:00Z"
    }
    ```

#### `GET` /api/opportunities/{id}
*   **Description:** Retrieves full opportunity details.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`

#### `GET` /api/opportunities
*   **Description:** Returns a paginated list of opportunities.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Query Parameters:**
    *   `stage`: (Optional) `PROSPECTING`, `PROPOSAL`, `NEGOTIATION`, `WON`, `LOST`
    *   `leadId`: (Optional) UUID
    *   `page`: (Optional, default `0`)
    *   `size`: (Optional, default `10`)

#### `GET` /api/opportunities/analytics/lost-analysis
*   **Description:** Aggregates lost opportunities grouped by the lost reason.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_MANAGER`
*   **Response (200 OK):**
    ```json
    [
      {
        "lostReason": "Competitor offered a lower price",
        "count": 2,
        "totalValue": 75000.00
      }
    ]
    ```

---

### 👤 1.4 Customer 360 Module (`/api/customers`)

#### `GET` /api/customers
*   **Description:** Lists all active customer accounts.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Response (200 OK):**
    ```json
    {
      "content": [
        {
          "id": "d0000000-0000-0000-0000-000000000001",
          "accountName": "Wayne Enterprises",
          "domainName": "waynecorp.com",
          "primaryEmail": "bruce@waynecorp.com",
          "phone": "+1-555-0100",
          "status": "ACTIVE"
        }
      ],
      "pageable": { ... }
    }
    ```

#### `GET` /api/customers/{id}
*   **Description:** Compiles a full 360-degree profile including links to won opportunities and total lifetime value (LTV).
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Response (200 OK):**
    ```json
    {
      "id": "d0000000-0000-0000-0000-000000000001",
      "accountName": "Wayne Enterprises",
      "domainName": "waynecorp.com",
      "primaryEmail": "bruce@waynecorp.com",
      "phone": "+1-555-0100",
      "status": "ACTIVE",
      "opportunityCount": 1,
      "totalLifetimeValue": 250000.00,
      "opportunities": [
        {
          "id": "f0000000-0000-0000-0000-000000000005",
          "title": "Wayne Enterprises Batmobile Fleet",
          "estimatedValue": 250000.00,
          "stage": "WON",
          "closedAt": "2026-07-21T23:30:00Z"
        }
      ]
    }
    ```

---

### 🔔 1.5 Notifications Module (`/api/notifications`)

#### `GET` /api/notifications
*   **Description:** Lists notifications for a recipient ID (e.g. assigned rep ID).
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`
*   **Query Parameters:**
    *   `recipientId`: (Required) UUID
    *   `page`: (Optional, default `0`)
    *   `size`: (Optional, default `10`)

#### `PATCH` /api/notifications/{id}/read
*   **Description:** Marks a notification as read.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_SALES_REP`, `ROLE_MANAGER`

---

### 📜 1.6 Audit Logs Module (`/api/audit-logs`)

#### `GET` /api/audit-logs
*   **Description:** Queries compliance and state changes across the entire sales pipeline.
*   **Role Access:** `ROLE_ADMIN`, `ROLE_MANAGER`
*   **Query Parameters:**
    *   `entityName`: (Optional) `LEAD`, `OPPORTUNITY`
    *   `entityId`: (Optional) UUID
    *   `action`: (Optional) e.g., `STAGE_CHANGE`, `STATUS_CHANGE`
    *   `page`: (Optional, default `0`)
    *   `size`: (Optional, default `10`)

---

## 🔄 2. End-to-End Sales Workflow Example

Below is the step-by-step data walkthrough for a lead that qualifies, gets assigned, converts to an opportunity, wins the deal, and establishes a customer account.

```
 [1] Intake Lead (Sync) ──► [2] Auto-Assignment (Kafka Async) ──► [3] Create Opp (Sync)
                                                                       │
 [6] Customer 360 ◄── [5] Dedup/Merge (Kafka Async) ◄── [4] Deal Won (Sync/State Machine)
```

### ➡️ Step 1: Lead Intake & Synchronous Scoring
*   **Endpoint:** `POST /api/leads`
*   **Auth:** Bearer Token (Jane - Sales Rep)
*   **Request Body:**
    ```json
    {
      "firstName": "Clark",
      "lastName": "Kent",
      "email": "clark.kent@dailyplanet.com",
      "phone": "+1-555-0700",
      "companyName": "Daily Planet",
      "companySize": "100-500",
      "leadSource": "WEBSITE"
    }
    ```
*   **Result:** Calculated Score: **85** (assigned via weights on source `WEBSITE` and size `100-500`).
*   **Event Emitted:** `lead.scored` topic is updated with payload containing lead ID `e9000000...` and score `85`.

---

### ➡️ Step 2: Asynchronous Rep Auto-Assignment
*   **Internal Handler:** `LeadAssignmentService` listens on `lead.scored`.
*   **Result:** Assigns the lead to the next sales rep in the round-robin queue (e.g. Rep `c0000000-0000-0000-0000-000000000002`).
*   **Audit Entry:**
    ```json
    {
      "entityName": "LEAD",
      "entityId": "e9000000...",
      "action": "STATUS_CHANGE",
      "oldState": "{\"status\":\"NEW\"}",
      "newState": "{\"status\":\"ASSIGNED\",\"assignedRepId\":\"c0000000-0000-0000-0000-000000000002\"}"
    }
    ```
*   **Notification Generated:** Rep `c0000000-0000-0000-0000-000000000002` receives a real-time `LEAD_ASSIGNED` notification.

---

### ➡️ Step 3: Create Opportunity
*   **Endpoint:** `POST /api/opportunities`
*   **Auth:** Bearer Token (Jane)
*   **Request Body:**
    ```json
    {
      "title": "Daily Planet Metropolis Bureau Upgrade",
      "leadId": "e9000000...",
      "estimatedValue": 80000.00
    }
    ```
*   **Result:** Opportunity is registered under state `PROSPECTING`.

---

### ➡️ Step 4: Deal Stage Progression & Closing (WON)
*   **Transition to Proposal:**
    *   `PATCH /api/opportunities/{oppId}/stage` -> Body: `{"stage": "PROPOSAL"}`
*   **Transition to Negotiation:**
    *   `PATCH /api/opportunities/{oppId}/stage` -> Body: `{"stage": "NEGOTIATION"}`
*   **Transition to Won:**
    *   `PATCH /api/opportunities/{oppId}/stage` -> Body: `{"stage": "WON"}`
*   **Event Emitted:** `opportunity.deal-won` Kafka event is fired.

---

### ➡️ Step 5: Asynchronous Customer Account Deduplication & Creation
*   **Internal Handler:** `DealWonConsumer` receives the event.
*   *   Queries existing accounts by domain extraction (`dailyplanet.com`).
    *   **If no domain match:** Creates a new Customer Account and links the opportunity.
    *   **If domain match:** Appends the opportunity to the existing domain account (data normalization).
*   **Status Update:** Lead status is automatically set to `CONVERTED`.

---

### ➡️ Step 6: View Customer 360 Degree View
*   **Endpoint:** `GET /api/customers/{customerId}`
*   **Auth:** Bearer Token (Manager)
*   **Response:**
    ```json
    {
      "id": "d1111111-1111-1111-1111-111111111111",
      "accountName": "Daily Planet",
      "domainName": "dailyplanet.com",
      "primaryEmail": "clark.kent@dailyplanet.com",
      "phone": "+1-555-0700",
      "status": "ACTIVE",
      "opportunityCount": 1,
      "totalLifetimeValue": 80000.00,
      "opportunities": [
        {
          "id": "f9999999-...",
          "title": "Daily Planet Metropolis Bureau Upgrade",
          "estimatedValue": 80000.00,
          "stage": "WON",
          "closedAt": "2026-07-21T23:31:00Z"
        }
      ]
    }
    ```
