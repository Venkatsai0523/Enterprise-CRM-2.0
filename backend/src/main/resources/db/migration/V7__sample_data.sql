-- Sample Data Seeding
-- Created for local development and testing

-- Password for users is 'password123' (BCrypt hash: $2a$10$Y50UaMFOxteibQEYofUXmO522XY2O8g.XZt82hpC/tZ5l3v/H50Ua)
INSERT INTO users (id, email, password_hash, first_name, last_name, enabled, created_at, updated_at) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'admin@nexus.com', '$2a$10$Y50UaMFOxteibQEYofUXmO522XY2O8g.XZt82hpC/tZ5l3v/H50Ua', 'System', 'Administrator', TRUE, TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-16 10:00:00'),
    ('c0000000-0000-0000-0000-000000000002', 'salesrep1@nexus.com', '$2a$10$Y50UaMFOxteibQEYofUXmO522XY2O8g.XZt82hpC/tZ5l3v/H50Ua', 'Alice', 'Sales', TRUE, TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-16 10:00:00'),
    ('c0000000-0000-0000-0000-000000000003', 'salesrep2@nexus.com', '$2a$10$Y50UaMFOxteibQEYofUXmO522XY2O8g.XZt82hpC/tZ5l3v/H50Ua', 'Bob', 'Representative', TRUE, TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-16 10:00:00'),
    ('c0000000-0000-0000-0000-000000000004', 'manager@nexus.com', '$2a$10$Y50UaMFOxteibQEYofUXmO522XY2O8g.XZt82hpC/tZ5l3v/H50Ua', 'Charles', 'Manager', TRUE, TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-16 10:00:00');

-- Assign Roles to Users
INSERT INTO user_roles (user_id, role_id) VALUES
    ('c0000000-0000-0000-0000-000000000001', 'b0000000-0000-0000-0000-000000000001'), -- admin -> ROLE_ADMIN
    ('c0000000-0000-0000-0000-000000000002', 'b0000000-0000-0000-0000-000000000002'), -- salesrep1 -> ROLE_SALES_REP
    ('c0000000-0000-0000-0000-000000000003', 'b0000000-0000-0000-0000-000000000002'), -- salesrep2 -> ROLE_SALES_REP
    ('c0000000-0000-0000-0000-000000000004', 'b0000000-0000-0000-0000-000000000003'); -- manager -> ROLE_MANAGER

-- Seed Leads
INSERT INTO leads (id, first_name, last_name, email, phone, company_name, company_size, lead_source, status, score, assigned_rep_id, created_at, updated_at) VALUES
    ('e0000000-0000-0000-0000-000000000001', 'John', 'Doe', 'john.doe@acme.com', '+1-555-0199', 'Acme Corp', '100-500', 'WEBSITE', 'ASSIGNED', 85, 'c0000000-0000-0000-0000-000000000002', TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-21 10:00:00'),
    ('e0000000-0000-0000-0000-000000000002', 'Jane', 'Smith', 'jane.smith@stark.com', '+1-555-0144', 'Stark Industries', '>500', 'REFERRAL', 'CONVERTED', 95, 'c0000000-0000-0000-0000-000000000002', TIMESTAMP '2026-07-11 10:00:00', TIMESTAMP '2026-07-21 10:00:00'),
    ('e0000000-0000-0000-0000-000000000003', 'Bruce', 'Wayne', 'bruce.wayne@wayne.com', '+1-555-0122', 'Wayne Enterprises', '>500', 'PARTNER', 'NEW', 40, NULL, TIMESTAMP '2026-07-20 10:00:00', TIMESTAMP '2026-07-21 10:00:00'),
    ('e0000000-0000-0000-0000-000000000004', 'Clark', 'Kent', 'clark.kent@dailyplanet.com', '+1-555-0133', 'Daily Planet', '10-50', 'WEBSITE', 'ASSIGNED', 65, 'c0000000-0000-0000-0000-000000000003', TIMESTAMP '2026-07-18 10:00:00', TIMESTAMP '2026-07-21 10:00:00');

-- Seed Opportunities
INSERT INTO opportunities (id, title, lead_id, estimated_value, stage, lost_reason, closed_at, created_at, updated_at) VALUES
    ('f0000000-0000-0000-0000-000000000001', 'Stark Arc Reactor Defense Contract', 'e0000000-0000-0000-0000-000000000002', 150000.00, 'WON', NULL, TIMESTAMP '2026-07-20 10:00:00', TIMESTAMP '2026-07-11 10:00:00', TIMESTAMP '2026-07-21 10:00:00'),
    ('f0000000-0000-0000-0000-000000000002', 'Acme Cloud Migration Project', 'e0000000-0000-0000-0000-000000000001', 45000.00, 'PROPOSAL', NULL, NULL, TIMESTAMP '2026-07-16 10:00:00', TIMESTAMP '2026-07-21 10:00:00'),
    ('f0000000-0000-0000-0000-000000000003', 'Daily Planet CRM License Deal', 'e0000000-0000-0000-0000-000000000004', 12500.00, 'NEGOTIATION', NULL, NULL, TIMESTAMP '2026-07-18 10:00:00', TIMESTAMP '2026-07-21 10:00:00');

-- Seed Customer Accounts
INSERT INTO customer_accounts (id, account_name, domain_name, primary_email, phone, status, created_at, updated_at) VALUES
    ('d0000000-0000-0000-0000-000000000001', 'Stark Industries', 'stark.com', 'jane.smith@stark.com', '+1-555-0144', 'ACTIVE', TIMESTAMP '2026-07-20 10:00:00', TIMESTAMP '2026-07-21 10:00:00');

-- Link Customer Opportunity
INSERT INTO customer_opportunities (id, customer_account_id, opportunity_id, linked_at) VALUES
    ('d0000000-0000-0000-0001-000000000001', 'd0000000-0000-0000-0000-000000000001', 'f0000000-0000-0000-0000-000000000001', TIMESTAMP '2026-07-20 10:00:00');

-- Seed Notifications
INSERT INTO notifications (id, recipient_id, type, message, is_read, created_at) VALUES
    ('10000000-0000-0000-0000-000000000001', 'c0000000-0000-0000-0000-000000000002', 'LEAD_ASSIGNED', 'New high-score lead assigned: John Doe (Acme Corp) - Score: 85', FALSE, TIMESTAMP '2026-07-16 10:00:00'),
    ('10000000-0000-0000-0000-000000000002', 'c0000000-0000-0000-0000-000000000002', 'DEAL_WON', 'Congratulations! Opportunity Stark Arc Reactor Defense Contract has been won.', FALSE, TIMESTAMP '2026-07-20 10:00:00');

-- Seed Audit Logs
INSERT INTO audit_logs (id, entity_name, entity_id, action, performed_by, old_state, new_state, timestamp) VALUES
    ('20000000-0000-0000-0000-000000000001', 'LEAD', 'e0000000-0000-0000-0000-000000000001', 'LEAD_CREATED', 'system', NULL, '{"status":"NEW","score":85}', TIMESTAMP '2026-07-16 10:00:00'),
    ('20000000-0000-0000-0000-000000000002', 'LEAD', 'e0000000-0000-0000-0000-000000000001', 'STATUS_CHANGE', 'salesrep1@nexus.com', '{"status":"NEW"}', '{"status":"ASSIGNED"}', TIMESTAMP '2026-07-16 10:00:00'),
    ('20000000-0000-0000-0000-000000000003', 'OPPORTUNITY', 'f0000000-0000-0000-0000-000000000001', 'STAGE_CHANGE', 'salesrep1@nexus.com', '{"stage":"NEGOTIATION"}', '{"stage":"WON"}', TIMESTAMP '2026-07-20 10:00:00');
