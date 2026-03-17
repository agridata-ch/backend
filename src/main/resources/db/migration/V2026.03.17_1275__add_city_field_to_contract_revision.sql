ALTER TABLE contract_revision
    ADD data_consumer_city VARCHAR(255);

ALTER TABLE contract_revision
    ALTER COLUMN data_consumer_city SET NOT NULL;