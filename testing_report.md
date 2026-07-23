# CRM 2.0 End-to-End Testing & Verification Report

This report documents the actual HTTP requests, JSON payloads, and responses executed during the end-to-end testing session for the multi-tenant Enterprise CRM 2.0 system.

* **Base URL**: `http://localhost:8080`
* **Tenant Organization**: CMREC College (Tenant ID: `96c047c1-daf6-43b7-91ef-9fd34f383a59`)

---

## 1. Organization & Admin Registration
Registers the organization `CMREC College` and maps the Admin user to the new tenant context.

* **HTTP Method**: `POST`
* **URL**: `/api/auth/register`
* **Request Body**:
  ```json
  {
    "email": "Mallesh.t@cmrec.ac.in",
    "password": "adminPassword123",
    "firstName": "Mallesh",
    "lastName": "T",
    "roleName": "ROLE_ADMIN",
    "organizationName": "CMREC College",
    "subdomain": "cmrec"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "771ee78d-194b-4a5f-b52b-7f37ccb8314e",
      "email": "Mallesh.t@cmrec.ac.in",
      "firstName": "Mallesh",
      "lastName": "T",
      "enabled": true,
      "roles": ["ROLE_ADMIN"],
      "createdAt": "2026-07-23T06:15:00.123Z"
    }
  }
  ```

---

## 2. Admin Invites Sales Representative
Creates a new team member (`rep1@cmrec.ac.in`) linked under the Admin's organization context.

* **HTTP Method**: `POST`
* **URL**: `/api/users`
* **Headers**: 
  * `Authorization: Bearer <admin_jwt_token>`
* **Request Body**:
  ```json
  {
    "email": "rep1@cmrec.ac.in",
    "password": "repPassword123",
    "firstName": "Suresh",
    "lastName": "Kumar",
    "roleName": "ROLE_SALES_REP"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "641c9a5a-2513-437a-9ee4-699ce3bc2c83",
      "email": "rep1@cmrec.ac.in",
      "firstName": "Suresh",
      "lastName": "Kumar",
      "enabled": true,
      "roles": ["ROLE_SALES_REP"],
      "createdAt": "2026-07-23T08:08:44.200Z"
    }
  }
  ```

---

## 3. Team Member Login
Authenticates `rep1` to obtain the Sales Representative's JWT token.

* **HTTP Method**: `POST`
* **URL**: `/api/auth/login`
* **Request Body**:
  ```json
  {
    "email": "rep1@cmrec.ac.in",
    "password": "repPassword123"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "token": "eyJhbGciOiJIUzI1NiJ9...",
      "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
      "type": "Bearer",
      "id": "641c9a5a-2513-437a-9ee4-699ce3bc2c83",
      "email": "rep1@cmrec.ac.in",
      "roles": ["ROLE_SALES_REP"]
    }
  }
  ```

---

## 4. Lead Intake & Dynamic Auto-Assignment
Creates a new lead. The system scores the lead (Score: 65) and assigns it to the representative carrying the fewest active leads.

* **HTTP Method**: `POST`
* **URL**: `/api/leads`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Request Body**:
  ```json
  {
    "firstName": "Naresh",
    "lastName": "Kumar",
    "email": "naresh.k@wipro.com",
    "companyName": "Wipro",
    "phone": "9848022338",
    "leadSource": "WEBSITE",
    "companySize": "100-500"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
      "firstName": "Naresh",
      "lastName": "Kumar",
      "email": "naresh.k@wipro.com",
      "phone": "9848022338",
      "companyName": "Wipro",
      "companySize": "100-500",
      "leadSource": "WEBSITE",
      "status": "ASSIGNED",
      "score": 65,
      "assignedRepId": "641c9a5a-2513-437a-9ee4-699ce3bc2c83",
      "createdAt": "2026-07-23T08:34:19.095574Z",
      "updatedAt": "2026-07-23T08:34:19.324151Z"
    }
  }
  ```

---

## 5. Promote Lead to Opportunity
Creates a pipeline opportunity starting in the `PROSPECTING` stage.

* **HTTP Method**: `POST`
* **URL**: `/api/opportunities`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Request Body**:
  ```json
  {
    "title": "Wipro Enterprise Accountproposal",
    "leadId": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
    "estimatedValue": 150000.00
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "c0186629-30d1-404e-bf73-e31201042915",
      "title": "Wipro Enterprise Accountproposal",
      "leadId": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
      "estimatedValue": 150000.00,
      "stage": "PROSPECTING",
      "lostReason": null,
      "closedAt": null,
      "createdAt": "2026-07-23T09:07:30.514434Z",
      "updatedAt": "2026-07-23T09:07:30.514434Z"
    }
  }
  ```

---

## 6. Mark Opportunity as WON (Triggers Customer Promotion)
Sets the stage to `WON` to trigger background Kafka consumers which generate a Customer Account.

* **HTTP Method**: `PATCH`
* **URL**: `/api/opportunities/c0186629-30d1-404e-bf73-e31201042915/stage`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Request Body**:
  ```json
  {
    "stage": "WON"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "c0186629-30d1-404e-bf73-e31201042915",
      "title": "Wipro Enterprise Accountproposal",
      "leadId": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
      "estimatedValue": 150000.00,
      "stage": "WON",
      "lostReason": null,
      "closedAt": "2026-07-23T09:09:07.518667200Z",
      "createdAt": "2026-07-23T09:07:30.514434Z",
      "updatedAt": "2026-07-23T09:09:07.518667Z"
    }
  }
  ```

---

## 7. Retrieve Customer Accounts
Confirms that the background listener successfully registered the Customer Account for Wipro.

* **HTTP Method**: `GET`
* **URL**: `/api/customers`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "content": [
        {
          "id": "487408ec-75b2-4dc9-98bd-616195c0fe7a",
          "accountName": "Wipro",
          "domainName": "wipro.com",
          "primaryEmail": "naresh.k@wipro.com",
          "phone": "9848022338",
          "status": "ACTIVE",
          "createdAt": "2026-07-23T09:09:07.587355Z",
          "updatedAt": "2026-07-23T09:09:07.587355Z"
        }
      ],
      "totalElements": 1,
      "totalPages": 1
    }
  }
  ```

---

## 8. Retrieve Customer 360-Degree View
Fetches full details including linked won opportunities and aggregated lifetime revenue.

* **HTTP Method**: `GET`
* **URL**: `/api/customers/487408ec-75b2-4dc9-98bd-616195c0fe7a`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "487408ec-75b2-4dc9-98bd-616195c0fe7a",
      "accountName": "Wipro",
      "domainName": "wipro.com",
      "primaryEmail": "naresh.k@wipro.com",
      "phone": "9848022338",
      "status": "ACTIVE",
      "totalLifetimeValue": 150000.00,
      "opportunityCount": 1,
      "linkedOpportunities": [
        {
          "id": "c0186629-30d1-404e-bf73-e31201042915",
          "title": "Wipro Enterprise Accountproposal",
          "leadId": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
          "estimatedValue": 150000.00,
          "stage": "WON"
        }
      ],
      "activities": []
    }
  }
  ```

---

## 9. Real-Time Lead Assignment Notifications
Confirms the assignment notification has been sent to the representative.

* **HTTP Method**: `GET`
* **URL**: `/api/notifications?recipientId=641c9a5a-2513-437a-9ee4-699ce3bc2c83`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "content": [
        {
          "id": "c1d3f6f6-fa1e-4bfc-b91f-7578a33f01c2",
          "recipientId": "641c9a5a-2513-437a-9ee4-699ce3bc2c83",
          "type": "LEAD_ASSIGNED",
          "message": "A new lead (ID: 8a23e07e-4713-4e70-a2dc-1536a6f5cf45, Score: 65) has been assigned to you.",
          "read": false,
          "createdAt": "2026-07-23T08:34:19.348362Z"
        }
      ]
    }
  }
  ```

---

## 10. Mark Notification as Read
* **HTTP Method**: `PATCH`
* **URL**: `/api/notifications/c1d3f6f6-fa1e-4bfc-b91f-7578a33f01c2/read`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "c1d3f6f6-fa1e-4bfc-b91f-7578a33f01c2",
      "read": true
    }
  }
  ```

---

## 11. Create Workflow Automation Rule
Admin registers a rule to schedule follow-up tasks automatically.

* **HTTP Method**: `POST`
* **URL**: `/api/workflows/rules`
* **Headers**:
  * `Authorization: Bearer <admin_jwt_token>`
* **Request Body**:
  ```json
  {
    "name": "High Score Lead Followup",
    "description": "Automatically schedules a task for leads with a score > 70",
    "triggerEvent": "LEAD_SCORED",
    "conditionsJson": "[{\"field\": \"score\", \"operator\": \"GREATER_THAN\", \"value\": \"70\"}]",
    "actionsJson": "[{\"type\": \"CREATE_TASK\", \"parameters\": {\"title\": \"Follow up with hot lead: {email}\", \"priority\": \"HIGH\", \"daysToDue\": \"2\"}}]"
  }
  ```
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "id": "7662c169-72c0-424a-b50a-5c1a1f0a29ef",
      "name": "High Score Lead Followup",
      "triggerEvent": "LEAD_SCORED",
      "active": true
    }
  }
  ```

---

## 12. Trigger Workflow Automation (Create Lead "Amit")
Creates a lead that scores 75 (triggering the follow-up task creation).

* **HTTP Method**: `POST`
* **URL**: `/api/leads`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Request Body**:
  ```json
  {
    "firstName": "Amit",
    "lastName": "Verma",
    "email": "amit.v@infosys.com",
    "companyName": "Infosys Ltd",
    "phone": "9898980000",
    "leadSource": "REFERRAL",
    "companySize": "100-500"
  }
  ```

---

## 13. Verify Scheduled Tasks
Confirms that the task was successfully auto-created by the engine.

* **HTTP Method**: `GET`
* **URL**: `/api/tasks`
* **Headers**:
  * `Authorization: Bearer <sales_rep_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "content": [
        {
          "id": "bc172b58-c440-4055-ad18-0c76de43cde8",
          "title": "Follow up with hot lead: amit.v@infosys.com",
          "description": "Automated workflow follow-up task.",
          "dueDate": "2026-07-25T09:37:19.975193Z",
          "priority": "HIGH",
          "status": "TODO",
          "type": "TASK",
          "assignedTo": "641c9a5a-2513-437a-9ee4-699ce3bc2c83",
          "relatedToType": "LEAD",
          "relatedToId": "a8711f69-38c2-4c7a-b896-65d9b71aa436",
          "createdAt": "2026-07-23T09:37:19.990469Z"
        }
      ]
    }
  }
  ```

---

## 14. Organization Analytics Dashboard
Retrieves organization-wide metrics.

* **HTTP Method**: `GET`
* **URL**: `/api/analytics/dashboard`
* **Headers**:
  * `Authorization: Bearer <admin_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "totalLeads": 7,
      "leadConversionRate": 14.285714285714285,
      "averageLeadScore": 78.57142857142857,
      "leadsBySource": {
        "REFERRAL": 2,
        "WEBSITE": 5
      },
      "totalPipelineValue": 235000.00,
      "activePipelineValue": 0,
      "dealWinRate": 100.0,
      "pipelineByStage": {
        "PROSPECTING": 0,
        "WON": 235000.00,
        "LOST": 0
      },
      "openTasks": 1,
      "overdueTasks": 0
    }
  }
  ```

---

## 15. Compliance Audit Logs
Queries mutating actions performed in the organization.

* **HTTP Method**: `GET`
* **URL**: `/api/audit-logs`
* **Headers**:
  * `Authorization: Bearer <admin_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": {
      "content": [
        {
          "id": "7f12d0a6-910b-4495-8ae0-7a731fa45528",
          "entityName": "TASK",
          "entityId": "bc172b58-c440-4055-ad18-0c76de43cde8",
          "action": "TASK_CREATED",
          "performedBy": "user",
          "newState": "TODO",
          "timestamp": "2026-07-23T09:37:19.993670Z"
        },
        {
          "id": "9916860f-82a0-4327-b6fd-e29bd225b7bd",
          "entityName": "LEAD",
          "entityId": "8a23e07e-4713-4e70-a2dc-1536a6f5cf45",
          "action": "STATUS_CHANGE",
          "performedBy": "system",
          "oldState": "ASSIGNED",
          "newState": "CONVERTED",
          "timestamp": "2026-07-23T09:09:07.596921Z"
        }
      ]
    }
  }
  ```

---

## 16. Lost Opportunity Analysis
* **HTTP Method**: `GET`
* **URL**: `/api/opportunities/analytics/lost-analysis`
* **Headers**:
  * `Authorization: Bearer <admin_jwt_token>`
* **Response**:
  ```json
  {
    "success": true,
    "message": "Operation completed successfully.",
    "data": []
  }
  ```
