-- Add seal state tracking fields to contract_revision table
ALTER TABLE contract_revision
    ADD COLUMN seal_state      VARCHAR(50),
    ADD COLUMN seal_started_at TIMESTAMP WITHOUT TIME ZONE;
