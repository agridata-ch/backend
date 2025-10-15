ALTER TABLE consent_request
    ADD data_producer_uid VARCHAR(50);

ALTER TABLE consent_request
    ALTER COLUMN data_producer_uid SET NOT NULL;

ALTER TABLE consent_request
    DROP COLUMN data_producer_id;
