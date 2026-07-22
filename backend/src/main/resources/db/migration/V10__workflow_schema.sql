CREATE TABLE workflow_rules (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    trigger_event VARCHAR(100) NOT NULL,
    conditions_json TEXT NOT NULL,
    actions_json TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    organization_id UUID NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workflow_rules_trigger_org ON workflow_rules(trigger_event, organization_id, active);
