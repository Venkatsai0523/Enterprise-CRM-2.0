-- Add soft delete support for Leads, Opportunities, and Tasks
-- Created for resolving OpenAPI gaps

ALTER TABLE leads ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE opportunities ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE tasks ADD COLUMN deleted_at TIMESTAMP WITH TIME ZONE;

-- Indexing for performance on non-deleted queries
CREATE INDEX idx_leads_deleted_at ON leads(deleted_at);
CREATE INDEX idx_opportunities_deleted_at ON opportunities(deleted_at);
CREATE INDEX idx_tasks_deleted_at ON tasks(deleted_at);
