ALTER TABLE data_request
    ADD consumer_signature_type VARCHAR(255);

ALTER TABLE data_request
    ADD provider_signature_type VARCHAR(255);

UPDATE data_request
SET consumer_signature_type = 'COLLECTIVE_SIGNATURE'
WHERE consumer_signature_type IS NULL;
ALTER TABLE data_request
    ALTER COLUMN consumer_signature_type SET NOT NULL;

UPDATE data_request
SET provider_signature_type = 'COLLECTIVE_SIGNATURE'
WHERE provider_signature_type IS NULL;
ALTER TABLE data_request
    ALTER COLUMN provider_signature_type SET NOT NULL;

ALTER TABLE contract_revision
    ADD consumer_signature_type VARCHAR(255);

ALTER TABLE contract_revision
    ADD provider_signature_type VARCHAR(255);