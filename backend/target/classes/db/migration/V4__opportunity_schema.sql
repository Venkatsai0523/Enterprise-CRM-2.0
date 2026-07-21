-- Opportunity Pipeline Domain Database Schema Migration
-- Created for Milestone 3

CREATE TABLE opportunities (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    lead_id UUID NOT NULL REFERENCES leads(id) ON DELETE CASCADE,
    estimated_value NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    stage VARCHAR(50) NOT NULL DEFAULT 'PROSPECTING',
    lost_reason VARCHAR(255),
    closed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_opportunities_stage ON opportunities(stage);
CREATE INDEX idx_opportunities_lead_id ON opportunities(lead_id);
CREATE INDEX idx_opportunities_created_at ON opportunities(created_at);
