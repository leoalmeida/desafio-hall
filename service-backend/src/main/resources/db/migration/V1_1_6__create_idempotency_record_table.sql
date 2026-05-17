CREATE TABLE IF NOT EXISTS IDEMPOTENCY_RECORD (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_name VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL,
    resource_id UUID NOT NULL,
    request_hash VARCHAR(255) NOT NULL,
    response_status INTEGER,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_idempotency_operation_key UNIQUE (operation_name, idempotency_key)
);
