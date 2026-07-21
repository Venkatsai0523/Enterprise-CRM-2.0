-- Lead Intelligence Domain Database Schema Migration
-- Created for Milestone 2

CREATE TABLE leads (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    company_name VARCHAR(255) NOT NULL,
    company_size VARCHAR(50),
    lead_source VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NEW',
    score INTEGER NOT NULL DEFAULT 0,
    assigned_rep_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_leads_status ON leads(status);
CREATE INDEX idx_leads_score ON leads(score);
CREATE INDEX idx_leads_assigned_rep_id ON leads(assigned_rep_id);
CREATE INDEX idx_leads_email ON leads(email);
