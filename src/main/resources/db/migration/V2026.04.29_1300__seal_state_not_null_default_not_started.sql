UPDATE contract_revision SET seal_state = 'NOT_STARTED' WHERE seal_state IS NULL;
ALTER TABLE contract_revision ALTER COLUMN seal_state SET NOT NULL;
ALTER TABLE contract_revision ALTER COLUMN seal_state SET DEFAULT 'NOT_STARTED';
