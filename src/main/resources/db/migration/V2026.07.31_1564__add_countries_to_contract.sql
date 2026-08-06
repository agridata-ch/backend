ALTER TABLE contract_revision
    ADD data_consumer_country VARCHAR(2);

ALTER TABLE contract_revision
    ADD data_provider_country VARCHAR(2);

UPDATE contract_revision
SET data_consumer_country = 'CH',
    data_provider_country = 'CH';

ALTER TABLE contract_revision
    ALTER COLUMN data_consumer_country SET NOT NULL;

ALTER TABLE contract_revision
    ALTER COLUMN data_provider_country SET NOT NULL;
