-- Customer 360 & Conversion Domain Database Schema Migration
-- Created for Milestone 4

CREATE TABLE customer_accounts (
    id UUID PRIMARY KEY,
    account_name VARCHAR(255) NOT NULL,
    domain_name VARCHAR(255) NOT NULL UNIQUE,
    primary_email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE customer_opportunities (
    id UUID PRIMARY KEY,
    customer_account_id UUID NOT NULL REFERENCES customer_accounts(id) ON DELETE CASCADE,
    opportunity_id UUID NOT NULL UNIQUE,
    linked_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_customer_accounts_domain_name ON customer_accounts(domain_name);
CREATE INDEX idx_customer_accounts_status ON customer_accounts(status);
CREATE INDEX idx_customer_opportunities_customer_id ON customer_opportunities(customer_account_id);
