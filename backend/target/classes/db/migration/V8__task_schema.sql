-- Migration for Task Domain
-- Created to support task management, activities, and calendars

CREATE TABLE tasks (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_date TIMESTAMP WITH TIME ZONE,
    priority VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(50) NOT NULL DEFAULT 'TODO',
    type VARCHAR(50) NOT NULL DEFAULT 'TASK',
    assigned_to UUID NOT NULL,
    related_to_type VARCHAR(100),
    related_to_id UUID,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_tasks_related_to ON tasks(related_to_type, related_to_id);
CREATE INDEX idx_tasks_status ON tasks(status);
