-- Baseline migration for Enterprise CRM 2.0
-- Created as part of Milestone 0: Infrastructure & Skeleton setup

CREATE TABLE IF NOT EXISTS system_metadata (
    id VARCHAR(50) PRIMARY KEY,
    property_key VARCHAR(100) NOT NULL,
    property_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO system_metadata (id, property_key, property_value)
VALUES ('meta-001', 'schema_version', '1.0.0');
