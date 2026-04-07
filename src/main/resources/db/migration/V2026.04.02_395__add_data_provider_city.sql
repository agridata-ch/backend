ALTER TABLE contract_revision
    ADD data_consumer_street VARCHAR(255);
UPDATE contract_revision
    SET data_consumer_street = ''
    WHERE data_consumer_street IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN data_consumer_street SET NOT NULL;

ALTER TABLE contract_revision
    ADD data_consumer_zip VARCHAR(10);
UPDATE contract_revision
    SET data_consumer_zip = ''
    WHERE data_consumer_zip IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN data_consumer_zip SET NOT NULL;

ALTER TABLE contract_revision
    ADD data_provider_street VARCHAR(255);
UPDATE contract_revision
    SET data_provider_street = ''
    WHERE data_provider_street IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN data_provider_street SET NOT NULL;

ALTER TABLE contract_revision
    ADD data_provider_zip VARCHAR(10);
UPDATE contract_revision
    SET data_provider_zip = ''
    WHERE data_provider_zip IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN data_provider_zip SET NOT NULL;

ALTER TABLE contract_revision
    ADD data_provider_city VARCHAR(255);
UPDATE contract_revision
    SET data_provider_city = ''
    WHERE data_provider_city IS NULL;
ALTER TABLE contract_revision
    ALTER COLUMN data_provider_city SET NOT NULL;



