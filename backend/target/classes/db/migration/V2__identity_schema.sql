-- Identity & Access Domain Database Schema Migration
-- Created for Milestone 1

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES permissions(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

-- Indexes for fast query lookup
CREATE INDEX idx_users_email ON users(email);

-- Seed initial system permissions and roles
INSERT INTO permissions (id, name, description) VALUES
    ('a0000000-0000-0000-0000-000000000001', 'USER_MANAGE', 'Permission to manage system users'),
    ('a0000000-0000-0000-0000-000000000002', 'LEAD_READ', 'Permission to read lead data'),
    ('a0000000-0000-0000-0000-000000000003', 'LEAD_WRITE', 'Permission to create and modify leads'),
    ('a0000000-0000-0000-0000-000000000004', 'DEAL_MANAGE', 'Permission to manage opportunity pipeline');

INSERT INTO roles (id, name, description) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'ROLE_ADMIN', 'Administrator role with full system access'),
    ('b0000000-0000-0000-0000-000000000002', 'ROLE_SALES_REP', 'Sales Representative role'),
    ('b0000000-0000-0000-0000-000000000003', 'ROLE_MANAGER', 'Sales Manager role'),
    ('b0000000-0000-0000-0000-000000000004', 'ROLE_USER', 'Standard authenticated user');

-- Assign permissions to roles
INSERT INTO role_permissions (role_id, permission_id) VALUES
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001'), -- ADMIN -> USER_MANAGE
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000002'), -- ADMIN -> LEAD_READ
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000003'), -- ADMIN -> LEAD_WRITE
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000004'), -- ADMIN -> DEAL_MANAGE
    ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000002'), -- SALES_REP -> LEAD_READ
    ('b0000000-0000-0000-0000-000000000002', 'a0000000-0000-0000-0000-000000000003'); -- SALES_REP -> LEAD_WRITE
