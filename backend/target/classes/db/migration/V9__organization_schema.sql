-- Create organizations table
CREATE TABLE organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    subdomain VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Seed default/bootstrap organization
-- Using a fixed UUID for the default organization: 'e8888888-8888-8888-8888-888888888888'
INSERT INTO organizations (id, name, subdomain, active) VALUES
    ('e8888888-8888-8888-8888-888888888888', 'Default Organization', 'default', TRUE);

-- Alter users table
ALTER TABLE users ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE users SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE users ALTER COLUMN organization_id SET NOT NULL;

-- Alter leads table
ALTER TABLE leads ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE leads SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE leads ALTER COLUMN organization_id SET NOT NULL;

-- Alter opportunities table
ALTER TABLE opportunities ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE opportunities SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE opportunities ALTER COLUMN organization_id SET NOT NULL;

-- Alter customer_accounts table
ALTER TABLE customer_accounts ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE customer_accounts SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE customer_accounts ALTER COLUMN organization_id SET NOT NULL;

-- Alter customer_opportunities table (link table)
ALTER TABLE customer_opportunities ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE customer_opportunities SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE customer_opportunities ALTER COLUMN organization_id SET NOT NULL;

-- Alter tasks table
ALTER TABLE tasks ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE tasks SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE tasks ALTER COLUMN organization_id SET NOT NULL;

-- Alter notifications table
ALTER TABLE notifications ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE notifications SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE notifications ALTER COLUMN organization_id SET NOT NULL;

-- Alter audit_logs table
ALTER TABLE audit_logs ADD COLUMN organization_id UUID REFERENCES organizations(id);
UPDATE audit_logs SET organization_id = 'e8888888-8888-8888-8888-888888888888';
ALTER TABLE audit_logs ALTER COLUMN organization_id SET NOT NULL;

-- Indexes for organization lookup
CREATE INDEX idx_users_org ON users(organization_id);
CREATE INDEX idx_leads_org ON leads(organization_id);
CREATE INDEX idx_opportunities_org ON opportunities(organization_id);
CREATE INDEX idx_customer_accounts_org ON customer_accounts(organization_id);
CREATE INDEX idx_customer_opportunities_org ON customer_opportunities(organization_id);
CREATE INDEX idx_tasks_org ON tasks(organization_id);
CREATE INDEX idx_notifications_org ON notifications(organization_id);
CREATE INDEX idx_audit_logs_org ON audit_logs(organization_id);
